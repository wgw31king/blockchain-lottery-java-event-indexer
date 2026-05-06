package com.example.blockchainlottery.application;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties;
import com.example.blockchainlottery.domain.ListenerState;
import com.example.blockchainlottery.infrastructure.persistence.ListenerStateRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListenerStateService {

    private final ListenerStateRepository listenerStateRepository;
    private final LotteryChainProperties properties;

    public ListenerStateService(ListenerStateRepository listenerStateRepository, LotteryChainProperties properties) {
        this.listenerStateRepository = listenerStateRepository;
        this.properties = properties;
    }

    @Transactional
    public ListenerState getOrCreateState() {
        return listenerStateRepository.findById(1L)
                .orElseGet(() -> listenerStateRepository.save(ListenerState.init(properties.getStartBlock())));
    }

    @Transactional
    public ListenerState updateAfterPolling(long processedBlock, long safeBlock) {
        ListenerState state = getOrCreateState();
        state.setLastProcessedBlock(processedBlock);
        state.setLastSafeBlock(safeBlock);
        state.setUpdatedAt(Instant.now());
        return listenerStateRepository.save(state);
    }

    @Transactional
    public ListenerState updateWsSeenBlock(long wsSeenBlock) {
        ListenerState state = getOrCreateState();
        state.setLastWsSeenBlock(Math.max(state.getLastWsSeenBlock(), wsSeenBlock));
        state.setUpdatedAt(Instant.now());
        return listenerStateRepository.save(state);
    }

    @Transactional
    public ListenerState resetStartBlock(long startBlock) {
        ListenerState state = getOrCreateState();
        state.setLastProcessedBlock(startBlock);
        state.setLastSafeBlock(startBlock);
        state.setLastWsSeenBlock(startBlock);
        state.setUpdatedAt(Instant.now());
        return listenerStateRepository.save(state);
    }
}
