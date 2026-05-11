package com.example.blockchainlottery.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    private List<String> additionalContractAddresses = new ArrayList<>();

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

    /**
     * Maximum blocks processed in one polling cycle (back-pressure for public RPCs).
     */
    @Min(1)
    private long maxBlocksPerPoll = 50L;

    @Min(1)
    private int maxRetryAttempts = 5;

    @Min(100)
    private long retryBackoffMs = 1000L;

    @Min(100)
    private long wsReconnectBackoffMs = 3000L;

    /**
     * Optional classpath or filesystem path to a contract ABI JSON array.
     */
    private String abiJsonPath;

    private boolean enableErc721Transfer;

    private boolean enableErc1155TransferSingle;

    private boolean enableErc1155TransferBatch;

    /**
     * When false, WebSocket and polling listeners are not started (default for API-only runs).
     */
    private boolean listenersEnabled = false;

    @Valid
    @NotEmpty
    private List<EventDefinition> eventDefinitions = new ArrayList<>();

    public List<String> allContractAddresses() {
        Set<String> set = new LinkedHashSet<>();
        set.add(contractAddress);
        for (String a : additionalContractAddresses) {
            if (a != null && !a.isBlank()) {
                set.add(a.trim());
            }
        }
        return List.copyOf(set);
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public List<String> getAdditionalContractAddresses() {
        return additionalContractAddresses;
    }

    public void setAdditionalContractAddresses(List<String> additionalContractAddresses) {
        this.additionalContractAddresses = additionalContractAddresses;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public long getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(long startBlock) {
        this.startBlock = startBlock;
    }

    public long getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(long confirmations) {
        this.confirmations = confirmations;
    }

    public long getReorgBufferBlocks() {
        return reorgBufferBlocks;
    }

    public void setReorgBufferBlocks(long reorgBufferBlocks) {
        this.reorgBufferBlocks = reorgBufferBlocks;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public long getMaxBlocksPerPoll() {
        return maxBlocksPerPoll;
    }

    public void setMaxBlocksPerPoll(long maxBlocksPerPoll) {
        this.maxBlocksPerPoll = maxBlocksPerPoll;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public long getWsReconnectBackoffMs() {
        return wsReconnectBackoffMs;
    }

    public void setWsReconnectBackoffMs(long wsReconnectBackoffMs) {
        this.wsReconnectBackoffMs = wsReconnectBackoffMs;
    }

    public String getAbiJsonPath() {
        return abiJsonPath;
    }

    public void setAbiJsonPath(String abiJsonPath) {
        this.abiJsonPath = abiJsonPath;
    }

    public boolean isEnableErc721Transfer() {
        return enableErc721Transfer;
    }

    public void setEnableErc721Transfer(boolean enableErc721Transfer) {
        this.enableErc721Transfer = enableErc721Transfer;
    }

    public boolean isEnableErc1155TransferSingle() {
        return enableErc1155TransferSingle;
    }

    public void setEnableErc1155TransferSingle(boolean enableErc1155TransferSingle) {
        this.enableErc1155TransferSingle = enableErc1155TransferSingle;
    }

    public boolean isEnableErc1155TransferBatch() {
        return enableErc1155TransferBatch;
    }

    public void setEnableErc1155TransferBatch(boolean enableErc1155TransferBatch) {
        this.enableErc1155TransferBatch = enableErc1155TransferBatch;
    }

    public boolean isListenersEnabled() {
        return listenersEnabled;
    }

    public void setListenersEnabled(boolean listenersEnabled) {
        this.listenersEnabled = listenersEnabled;
    }

    public List<EventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }

    public void setEventDefinitions(List<EventDefinition> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
    }

    public static class EventDefinition {
        @NotBlank
        private String name;

        @NotBlank
        private String signature;

        private List<String> indexedParams = new ArrayList<>();

        private List<String> dataParams = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public List<String> getIndexedParams() {
            return indexedParams;
        }

        public void setIndexedParams(List<String> indexedParams) {
            this.indexedParams = indexedParams;
        }

        public List<String> getDataParams() {
            return dataParams;
        }

        public void setDataParams(List<String> dataParams) {
            this.dataParams = dataParams;
        }
    }
}
