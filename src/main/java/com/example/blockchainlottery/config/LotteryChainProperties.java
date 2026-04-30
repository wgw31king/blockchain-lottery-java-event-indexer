package com.example.blockchainlottery.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "lottery.chain")
public class LotteryChainProperties {

    @NotBlank
    private String rpcUrl;

    @NotBlank
    private String wsUrl;

    @NotBlank
    private String contractAddress;

    @NotBlank
    private String chainId;

    @Min(0)
    private long startBlock = 0L;

    @Min(1)
    private long confirmations = 6L;

    @Min(0)
    private long reorgBufferBlocks = 24L;

    @Min(1000)
    private long pollIntervalMs = 10000L;

    @Min(1)
    private int maxRetryAttempts = 5;

    @Min(100)
    private long retryBackoffMs = 1000L;

    @Min(100)
    private long wsReconnectBackoffMs = 3000L;

    @Valid
    @NotEmpty
    private List<EventDefinition> eventDefinitions = new ArrayList<>();

    public static class EventDefinition {
        @NotBlank
        private String name;

        @NotBlank
        private String signature;

        private List<String> indexedParams = new ArrayList<>();

        private List<String> dataParams = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public List<String> getIndexedParams() { return indexedParams; }
        public void setIndexedParams(List<String> indexedParams) { this.indexedParams = indexedParams; }
        public List<String> getDataParams() { return dataParams; }
        public void setDataParams(List<String> dataParams) { this.dataParams = dataParams; }
    }

    public String getRpcUrl() { return rpcUrl; }
    public void setRpcUrl(String rpcUrl) { this.rpcUrl = rpcUrl; }
    public String getWsUrl() { return wsUrl; }
    public void setWsUrl(String wsUrl) { this.wsUrl = wsUrl; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
    public long getStartBlock() { return startBlock; }
    public void setStartBlock(long startBlock) { this.startBlock = startBlock; }
    public long getConfirmations() { return confirmations; }
    public void setConfirmations(long confirmations) { this.confirmations = confirmations; }
    public long getReorgBufferBlocks() { return reorgBufferBlocks; }
    public void setReorgBufferBlocks(long reorgBufferBlocks) { this.reorgBufferBlocks = reorgBufferBlocks; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    public long getRetryBackoffMs() { return retryBackoffMs; }
    public void setRetryBackoffMs(long retryBackoffMs) { this.retryBackoffMs = retryBackoffMs; }
    public long getWsReconnectBackoffMs() { return wsReconnectBackoffMs; }
    public void setWsReconnectBackoffMs(long wsReconnectBackoffMs) { this.wsReconnectBackoffMs = wsReconnectBackoffMs; }
    public List<EventDefinition> getEventDefinitions() { return eventDefinitions; }
    public void setEventDefinitions(List<EventDefinition> eventDefinitions) { this.eventDefinitions = eventDefinitions; }
}
