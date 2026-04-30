package com.example.blockchainlottery.service;

import com.example.blockchainlottery.domain.ListenerState;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BlockchainEventListenerService {

    private final ListenerStateService listenerStateService;

    public BlockchainEventListenerService(ListenerStateService listenerStateService) {
        this.listenerStateService = listenerStateService;
    }

    public ListenerState getListenerState() {
        return listenerStateService.getOrCreateState();
    }

    public ListenerState resetListener(long fromBlock) {
        return listenerStateService.resetStartBlock(fromBlock);
    }

    public Map<String, Object> health() {
        ListenerState state = getListenerState();
        return Map.of(
                "status", "UP",
                "lastProcessedBlock", state.getLastProcessedBlock(),
                "lastSafeBlock", state.getLastSafeBlock(),
                "lastWsSeenBlock", state.getLastWsSeenBlock(),
                "updatedAt", Instant.now().toString()
        );
    }
}
