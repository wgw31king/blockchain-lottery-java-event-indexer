package com.example.blockchainlottery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "blockchain_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chain_tx_log", columnNames = {"chain_id", "tx_hash", "log_index"})
        },
        indexes = {
                @Index(name = "idx_event_block", columnList = "chain_id,block_number"),
                @Index(name = "idx_event_name", columnList = "event_name"),
                @Index(name = "idx_event_from", columnList = "from_address"),
                @Index(name = "idx_event_to", columnList = "to_address")
        })
public class BlockchainEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_id", nullable = false, length = 40)
    private String chainId;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "block_hash", nullable = false, length = 80)
    private String blockHash;

    @Column(name = "tx_hash", nullable = false, length = 80)
    private String txHash;

    @Column(name = "log_index", nullable = false)
    private Long logIndex;

    @Column(name = "contract_address", nullable = false, length = 60)
    private String contractAddress;

    @Column(name = "topic0", nullable = false, length = 80)
    private String topic0;

    @Column(name = "event_signature", nullable = false, length = 255)
    private String eventSignature;

    @Column(name = "event_name", nullable = false, length = 120)
    private String eventName;

    @Column(name = "from_address", length = 60)
    private String fromAddress;

    @Column(name = "to_address", length = 60)
    private String toAddress;

    @Column(name = "removed", nullable = false)
    private boolean removed;

    @Column(name = "raw_data", nullable = false, columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "decoded_params_json", nullable = false, columnDefinition = "TEXT")
    private String decodedParamsJson;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public Long getId() { return id; }
    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public Long getLogIndex() { return logIndex; }
    public void setLogIndex(Long logIndex) { this.logIndex = logIndex; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public String getTopic0() { return topic0; }
    public void setTopic0(String topic0) { this.topic0 = topic0; }
    public String getEventSignature() { return eventSignature; }
    public void setEventSignature(String eventSignature) { this.eventSignature = eventSignature; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
    public String getDecodedParamsJson() { return decodedParamsJson; }
    public void setDecodedParamsJson(String decodedParamsJson) { this.decodedParamsJson = decodedParamsJson; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
