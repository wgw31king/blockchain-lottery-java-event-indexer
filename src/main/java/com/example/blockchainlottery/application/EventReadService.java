package com.example.blockchainlottery.application;

import com.example.blockchainlottery.domain.BlockchainEvent;
import com.example.blockchainlottery.infrastructure.persistence.BlockchainEventRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class EventReadService {

    private final BlockchainEventRepository eventRepository;

    public EventReadService(BlockchainEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Cacheable(
            cacheNames = "eventPages",
            key = "(#eventName ?: '') + '|' + (#fromAddress ?: '') + '|' + (#toAddress ?: '')"
                    + "+ '|' + (#fromBlock ?: '') + '|' + (#toBlock ?: '') + '|' + #page + '|' + #size"
    )
    public Page<BlockchainEvent> findEvents(
            String eventName,
            String fromAddress,
            String toAddress,
            Long fromBlock,
            Long toBlock,
            int page,
            int size
    ) {
        Specification<BlockchainEvent> spec = Specification.where(null);
        if (eventName != null && !eventName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventName"), eventName));
        }
        if (fromAddress != null && !fromAddress.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("fromAddress"), fromAddress));
        }
        if (toAddress != null && !toAddress.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("toAddress"), toAddress));
        }
        if (fromBlock != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("blockNumber"), fromBlock));
        }
        if (toBlock != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("blockNumber"), toBlock));
        }
        return eventRepository.findAll(spec, PageRequest.of(page, Math.min(size, 100)));
    }
}
