package com.example.blockchainlottery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "listener_state")
public class ListenerState {

    @Id
    private Long id;

    @Column(name = "last_processed_block", nullable = false)
    private Long lastProcessedBlock;

    @Column(name = "last_safe_block", nullable = false)
    private Long lastSafeBlock;

    @Column(name = "last_ws_seen_block", nullable = false)
    private Long lastWsSeenBlock;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ListenerState init(long startBlock) {
        ListenerState state = new ListenerState();
        state.id = 1L;
        state.lastProcessedBlock = startBlock;
        state.lastSafeBlock = startBlock;
        state.lastWsSeenBlock = startBlock;
        state.updatedAt = Instant.now();
        return state;
    }

    public Long getId() { return id; }
    public Long getLastProcessedBlock() { return lastProcessedBlock; }
    public void setLastProcessedBlock(Long lastProcessedBlock) { this.lastProcessedBlock = lastProcessedBlock; }
    public Long getLastSafeBlock() { return lastSafeBlock; }
    public void setLastSafeBlock(Long lastSafeBlock) { this.lastSafeBlock = lastSafeBlock; }
    public Long getLastWsSeenBlock() { return lastWsSeenBlock; }
    public void setLastWsSeenBlock(Long lastWsSeenBlock) { this.lastWsSeenBlock = lastWsSeenBlock; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
