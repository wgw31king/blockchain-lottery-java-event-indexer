package com.example.blockchainlottery.application;

import com.example.blockchainlottery.infrastructure.alerting.AlertNotifier;
import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties;
import com.example.blockchainlottery.infrastructure.metrics.IndexerErrorMetrics;
import com.example.blockchainlottery.infrastructure.decoder.AbiEventDecoderRegistry;
import com.example.blockchainlottery.infrastructure.util.DebugNdjsonLogger;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.websocket.WebSocketService;

@Service
public class WebSocketSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSubscriptionService.class);

    private final Web3j wsWeb3j;
    private final WebSocketService webSocketService;
    private final LotteryChainProperties properties;
    private final EventIngestionService eventIngestionService;
    private final ListenerStateService listenerStateService;
    private final AbiEventDecoderRegistry decoderRegistry;
    private final IndexerErrorMetrics indexerErrorMetrics;
    private final AlertNotifier alertNotifier;
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile Disposable subscription;

    public WebSocketSubscriptionService(
            @Qualifier("wsWeb3j") Web3j wsWeb3j,
            @Qualifier("webSocketService") WebSocketService webSocketService,
            LotteryChainProperties properties,
            EventIngestionService eventIngestionService,
            ListenerStateService listenerStateService,
            AbiEventDecoderRegistry decoderRegistry,
            IndexerErrorMetrics indexerErrorMetrics,
            AlertNotifier alertNotifier
    ) {
        this.wsWeb3j = wsWeb3j;
        this.webSocketService = webSocketService;
        this.properties = properties;
        this.eventIngestionService = eventIngestionService;
        this.listenerStateService = listenerStateService;
        this.decoderRegistry = decoderRegistry;
        this.indexerErrorMetrics = indexerErrorMetrics;
        this.alertNotifier = alertNotifier;
    }

    @PostConstruct
    public void start() {
        if (!properties.isListenersEnabled()) {
            log.info("WebSocket listener disabled via configuration");
            return;
        }
        subscribe();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        reconnectExecutor.shutdownNow();
    }

    private synchronized void subscribe() {
        try {
            try {
                webSocketService.connect();
            } catch (Exception ignored) {
                log.debug("WS connect skipped: {}", ignored.getMessage());
            }

            EthFilter filter = new EthFilter(
                    DefaultBlockParameterName.LATEST,
                    DefaultBlockParameterName.LATEST,
                    properties.allContractAddresses()
            );
            List<String> topics = decoderRegistry.supportedTopics();
            if (!topics.isEmpty()) {
                filter.addOptionalTopics(topics.toArray(new String[0]));
            }

            subscription = wsWeb3j.ethLogFlowable(filter)
                    .doOnError(error -> {
                        log.error("WS subscription failed", error);
                        indexerErrorMetrics.increment("ws");
                        alertNotifier.notify("Indexer websocket error", error.getMessage());
                        scheduleReconnect();
                    })
                    .retryWhen(errors -> errors.delay(properties.getWsReconnectBackoffMs(), TimeUnit.MILLISECONDS))
                    .subscribe(this::consumeLog, error -> {
                        log.error("WS stream error", error);
                        indexerErrorMetrics.increment("ws");
                        alertNotifier.notify("Indexer websocket stream error", error.getMessage());
                        scheduleReconnect();
                    });

            log.info("WebSocket subscription started");
        } catch (Exception e) {
            log.error("WebSocket connect failed", e);
            indexerErrorMetrics.increment("ws");
            alertNotifier.notify("Indexer websocket connect failed", e.getMessage());
            scheduleReconnect();
        }
    }

    private void consumeLog(Log logObject) {
        DebugNdjsonLogger.log(
                "compile-run",
                "H2",
                "WebSocketSubscriptionService.consumeLog",
                "consume websocket log",
                "{\"hasBlockNumber\":" + (logObject.getBlockNumber() != null) + "}"
        );
        try {
            eventIngestionService.ingest(logObject, "websocket");
        } catch (Exception ex) {
            log.error("WS ingest failed", ex);
            indexerErrorMetrics.increment("ingest");
            alertNotifier.notify("Indexer ingest failure", ex.getMessage());
        }
        long block = logObject.getBlockNumber().longValue();
        listenerStateService.updateWsSeenBlock(block);
    }

    private void scheduleReconnect() {
        reconnectExecutor.schedule(this::subscribe, properties.getWsReconnectBackoffMs(), TimeUnit.MILLISECONDS);
    }
}
