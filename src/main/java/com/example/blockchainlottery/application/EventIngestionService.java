package com.example.blockchainlottery.application;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties;
import com.example.blockchainlottery.domain.BlockchainEvent;
import com.example.blockchainlottery.domain.TransactionDetail;
import com.example.blockchainlottery.infrastructure.persistence.BlockchainEventRepository;
import com.example.blockchainlottery.infrastructure.persistence.TransactionDetailRepository;
import com.example.blockchainlottery.infrastructure.decoder.AbiEventDecoderRegistry;
import com.example.blockchainlottery.infrastructure.decoder.DecodedEvent;
import com.example.blockchainlottery.infrastructure.util.DebugNdjsonLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;

@Service
public class EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final Web3j httpWeb3j;
    private final LotteryChainProperties properties;
    private final BlockchainEventRepository eventRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final RetryExecutor retryExecutor;
    private final AbiEventDecoderRegistry decoderRegistry;
    private final ObjectMapper objectMapper;
    private final Counter ingestCounter;

    public EventIngestionService(
            @Qualifier("httpWeb3j") Web3j httpWeb3j,
            LotteryChainProperties properties,
            BlockchainEventRepository eventRepository,
            TransactionDetailRepository transactionDetailRepository,
            RetryExecutor retryExecutor,
            AbiEventDecoderRegistry decoderRegistry,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.httpWeb3j = httpWeb3j;
        this.properties = properties;
        this.eventRepository = eventRepository;
        this.transactionDetailRepository = transactionDetailRepository;
        this.retryExecutor = retryExecutor;
        this.decoderRegistry = decoderRegistry;
        this.objectMapper = objectMapper;
        this.ingestCounter = meterRegistry.counter("lottery_event_ingested_total");
    }

    @Transactional
    public void ingest(Log logObject, String source) {
        // #region agent log
        DebugNdjsonLogger.log(
                "compile-run",
                "H2",
                "EventIngestionService.ingest",
                "ingest value snapshot",
                "{\"source\":\"" + source + "\",\"hasBlockNumber\":" + (logObject.getBlockNumber() != null)
                        + ",\"hasLogIndex\":" + (logObject.getLogIndex() != null) + "}"
        );
        // #endregion
        Long logIndex = logObject.getLogIndex().longValue();
        String txHash = logObject.getTransactionHash();
        String chainId = properties.getChainId();

        if (eventRepository.findByChainIdAndTxHashAndLogIndex(chainId, txHash, logIndex).isPresent()) {
            return;
        }

        DecodedEvent decoded = decoderRegistry.decode(logObject);

        BlockchainEvent event = new BlockchainEvent();
        event.setChainId(chainId);
        event.setBlockNumber(logObject.getBlockNumber().longValue());
        event.setBlockHash(logObject.getBlockHash());
        event.setTxHash(txHash);
        event.setLogIndex(logIndex);
        event.setContractAddress(logObject.getAddress());
        event.setTopic0(logObject.getTopics() == null || logObject.getTopics().isEmpty() ? "" : logObject.getTopics().get(0));
        event.setEventName(decoded.name());
        event.setEventSignature(decoded.signature());
        event.setFromAddress(decoded.fromAddress());
        event.setToAddress(decoded.toAddress());
        event.setRemoved(logObject.isRemoved());
        event.setRawData(logObject.getData());
        event.setDecodedParamsJson(toJson(decoded.decodedParams()));
        event.setProcessedAt(Instant.now());

        eventRepository.save(event);
        upsertTransactionDetail(logObject, chainId);
        ingestCounter.increment();

        log.info("Ingested event source={} chain={} block={} txHash={} idx={} event={}", source, chainId,
                event.getBlockNumber(), txHash, logIndex, decoded.name());
    }

    private void upsertTransactionDetail(Log logObject, String chainId) {
        String txHash = String.valueOf(logObject.getTransactionHash());
        Optional<TransactionDetail> existing = transactionDetailRepository.findById(txHash);

        Optional<Transaction> tx = retryExecutor.execute("eth_getTransactionByHash", () -> {
            try {
                EthTransaction response = httpWeb3j.ethGetTransactionByHash(txHash).send();
                if (response.hasError()) {
                    String errorMessage = "eth_getTransactionByHash error";
                    if (response.getError() != null && response.getError().getMessage() != null) {
                        errorMessage = response.getError().getMessage();
                    }
                    throw new IllegalStateException(errorMessage);
                }
                return response.getTransaction();
            } catch (Exception e) {
                throw new IllegalStateException("eth_getTransactionByHash failed", e);
            }
        });

        tx.ifPresent(transaction -> {
            TransactionDetail detail = existing.orElseGet(TransactionDetail::new);
            detail.setTxHash(txHash);
            detail.setChainId(chainId);
            detail.setBlockNumber(transaction.getBlockNumber() == null ? null : transaction.getBlockNumber().longValue());
            detail.setFromAddress(transaction.getFrom());
            detail.setToAddress(transaction.getTo());
            detail.setTxValue(transaction.getValue());
            detail.setGas(transaction.getGas());
            detail.setGasPrice(transaction.getGasPrice());
            detail.setStatus("UNKNOWN");
            detail.setFetchedAt(Instant.now());
            transactionDetailRepository.save(detail);
        });
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize decoded params", e);
        }
    }
}
