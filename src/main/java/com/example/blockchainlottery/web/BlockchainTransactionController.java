package com.example.blockchainlottery.web;

import com.example.blockchainlottery.domain.BlockchainEvent;
import com.example.blockchainlottery.domain.ListenerState;
import com.example.blockchainlottery.repository.BlockchainEventRepository;
import com.example.blockchainlottery.service.BlockchainEventListenerService;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class BlockchainTransactionController {

    private final BlockchainEventRepository eventRepository;
    private final BlockchainEventListenerService listenerService;

    public BlockchainTransactionController(
            BlockchainEventRepository eventRepository,
            BlockchainEventListenerService listenerService
    ) {
        this.eventRepository = eventRepository;
        this.listenerService = listenerService;
    }

    @GetMapping("/events")
    public Page<BlockchainEvent> listEvents(
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) String fromAddress,
            @RequestParam(required = false) String toAddress,
            @RequestParam(required = false) @Min(0) Long fromBlock,
            @RequestParam(required = false) @Min(0) Long toBlock,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
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

    @GetMapping("/events/{txHash}/{logIndex}")
    public BlockchainEvent getOne(@PathVariable String txHash, @PathVariable Long logIndex) {
        return eventRepository.findFirstByTxHashIgnoreCaseAndLogIndex(txHash, logIndex)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @GetMapping("/listener/state")
    public ListenerState listenerState() {
        return listenerService.getListenerState();
    }

    @GetMapping("/listener/health")
    public Map<String, Object> listenerHealth() {
        return listenerService.health();
    }

    @PostMapping("/listener/reset")
    public Map<String, Object> resetListener(@RequestParam @Min(0) long fromBlock) {
        ListenerState state = listenerService.resetListener(fromBlock);
        return Map.of(
                "message", "listener reset success",
                "lastProcessedBlock", state.getLastProcessedBlock(),
                "lastSafeBlock", state.getLastSafeBlock(),
                "lastWsSeenBlock", state.getLastWsSeenBlock(),
                "updatedAt", state.getUpdatedAt().toString()
        );
    }
}
