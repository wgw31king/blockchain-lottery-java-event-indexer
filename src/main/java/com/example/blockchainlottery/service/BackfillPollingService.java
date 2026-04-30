package com.example.blockchainlottery.service;

import com.example.blockchainlottery.config.LotteryChainProperties;
import com.example.blockchainlottery.domain.ListenerState;
import com.example.blockchainlottery.repository.BlockchainEventRepository;
import com.example.blockchainlottery.service.decoder.AbiEventDecoderRegistry;
import com.example.blockchainlottery.util.DebugNdjsonLogger;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

@Service
public class BackfillPollingService {

    private static final Logger log = LoggerFactory.getLogger(BackfillPollingService.class);

    private final Web3j httpWeb3j;
    private final LotteryChainProperties properties;
    private final RetryExecutor retryExecutor;
    private final ListenerStateService listenerStateService;
    private final EventIngestionService eventIngestionService;
    private final AbiEventDecoderRegistry decoderRegistry;
    private final BlockchainEventRepository eventRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public BackfillPollingService(
            @Qualifier("httpWeb3j") Web3j httpWeb3j,
            LotteryChainProperties properties,
            RetryExecutor retryExecutor,
            ListenerStateService listenerStateService,
            EventIngestionService eventIngestionService,
            AbiEventDecoderRegistry decoderRegistry,
            BlockchainEventRepository eventRepository
    ) {
        this.httpWeb3j = httpWeb3j;
        this.properties = properties;
        this.retryExecutor = retryExecutor;
        this.listenerStateService = listenerStateService;
        this.eventIngestionService = eventIngestionService;
        this.decoderRegistry = decoderRegistry;
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedDelayString = "${lottery.chain.poll-interval-ms:10000}")
    public void pollConfirmedBlocks() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            doPoll();
        } catch (Exception ex) {
            log.error("Polling backfill failed", ex);
        } finally {
            running.set(false);
        }
    }

    @Transactional
    protected void doPoll() {
        ListenerState state = listenerStateService.getOrCreateState();
        long latest = latestBlock();
        long safeBlock = Math.max(properties.getStartBlock(), latest - properties.getConfirmations());
        long from = Math.max(properties.getStartBlock(), state.getLastProcessedBlock() - properties.getReorgBufferBlocks());

        if (safeBlock < from) {
            return;
        }

        eventRepository.deleteByChainIdAndBlockNumberGreaterThanEqual(properties.getChainId(), from);

        for (long block = from; block <= safeBlock; block++) {
            List<EthLog.LogResult<?>> logs = fetchBlockLogs(block);
            for (EthLog.LogResult<?> logResult : logs) {
                eventIngestionService.ingest((Log) logResult.get(), "polling");
            }
        }

        listenerStateService.updateAfterPolling(safeBlock, safeBlock);
    }

    private long latestBlock() {
        return retryExecutor.execute("eth_blockNumber", () -> {
            try {
                return httpWeb3j.ethBlockNumber().send().getBlockNumber().longValue();
            } catch (Exception e) {
                throw new IllegalStateException("eth_blockNumber failed", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<EthLog.LogResult<?>> fetchBlockLogs(long blockNumber) {
        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                properties.getContractAddress()
        );
        List<String> topics = decoderRegistry.supportedTopics();
        if (!topics.isEmpty()) {
            filter.addOptionalTopics(topics.toArray(new String[0]));
        }

        return retryExecutor.execute("eth_getLogs", () -> {
            try {
                // #region agent log
                DebugNdjsonLogger.log(
                        "compile-run",
                        "H1",
                        "BackfillPollingService.fetchBlockLogs",
                        "sending eth_getLogs request",
                        "{\"blockNumber\":" + blockNumber + "}"
                );
                // #endregion
                EthLog response = httpWeb3j.ethGetLogs(filter).send();
                if (response.hasError()) {
                    throw new IllegalStateException(response.getError().getMessage());
                }
                // #region agent log
                DebugNdjsonLogger.log(
                        "compile-run",
                        "H1",
                        "BackfillPollingService.fetchBlockLogs",
                        "eth_getLogs response received",
                        "{\"logCount\":" + response.getLogs().size() + "}"
                );
                // #endregion
                return (List<EthLog.LogResult<?>>) (List<?>) response.getLogs();
            } catch (Exception e) {
                throw new IllegalStateException("eth_getLogs failed", e);
            }
        });
    }
}
