package com.example.blockchainlottery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(name = "transaction_details",
        indexes = {
                @Index(name = "idx_tx_detail_chain_block", columnList = "chain_id,block_number")
        })
public class TransactionDetail {

    @Id
    @Column(name = "tx_hash", length = 80)
    private String txHash;

    @Column(name = "chain_id", nullable = false, length = 40)
    private String chainId;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "from_address", length = 60)
    private String fromAddress;

    @Column(name = "to_address", length = 60)
    private String toAddress;

    @Column(name = "tx_value", precision = 65, scale = 0)
    private BigInteger txValue;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "gas", precision = 65, scale = 0)
    private BigInteger gas;

    @Column(name = "gas_price", precision = 65, scale = 0)
    private BigInteger gasPrice;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public BigInteger getTxValue() { return txValue; }
    public void setTxValue(BigInteger txValue) { this.txValue = txValue; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigInteger getGas() { return gas; }
    public void setGas(BigInteger gas) { this.gas = gas; }
    public BigInteger getGasPrice() { return gasPrice; }
    public void setGasPrice(BigInteger gasPrice) { this.gasPrice = gasPrice; }
    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
}
