package com.example.blockchainlottery.interfaces.web;

import com.example.blockchainlottery.infrastructure.config.ApiProperties;
import com.example.blockchainlottery.domain.BlockchainEvent;
import com.example.blockchainlottery.domain.ListenerState;
import com.example.blockchainlottery.infrastructure.persistence.BlockchainEventRepository;
import com.example.blockchainlottery.infrastructure.security.ReplayProtectionService;
import com.example.blockchainlottery.application.BlockchainEventListenerService;
import com.example.blockchainlottery.application.EventReadService;
import com.example.blockchainlottery.interfaces.web.dto.BlockchainEventResponse;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class BlockchainTransactionController {

    private final EventReadService eventReadService;
    private final BlockchainEventRepository eventRepository;
    private final BlockchainEventListenerService listenerService;
    private final ReplayProtectionService replayProtectionService;
    private final ApiProperties apiProperties;

    public BlockchainTransactionController(
            EventReadService eventReadService,
            BlockchainEventRepository eventRepository,
            BlockchainEventListenerService listenerService,
            ReplayProtectionService replayProtectionService,
            ApiProperties apiProperties
    ) {
        this.eventReadService = eventReadService;
        this.eventRepository = eventRepository;
        this.listenerService = listenerService;
        this.replayProtectionService = replayProtectionService;
        this.apiProperties = apiProperties;
    }

    @GetMapping("/events")
    public Page<BlockchainEventResponse> listEvents(
            @RequestParam(required = false) String eventName,
            @RequestParam(required = false) String fromAddress,
            @RequestParam(required = false) String toAddress,
            @RequestParam(required = false) @Min(0) Long fromBlock,
            @RequestParam(required = false) @Min(0) Long toBlock,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        Page<BlockchainEvent> events = eventReadService.findEvents(
                eventName, fromAddress, toAddress, fromBlock, toBlock, page, size
        );
        return events.map(e -> BlockchainEventResponse.from(e, apiProperties.isMaskAddresses()));
    }

    @GetMapping("/events/{txHash}/{logIndex}")
    public BlockchainEventResponse getOne(@PathVariable String txHash, @PathVariable Long logIndex) {
        BlockchainEvent e = eventRepository.findFirstByTxHashIgnoreCaseAndLogIndex(txHash, logIndex)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return BlockchainEventResponse.from(e, apiProperties.isMaskAddresses());
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
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> resetListener(
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @RequestParam @Min(0) long fromBlock
    ) {
        if (!replayProtectionService.registerIdempotencyKey(idempotencyKey)) {
            throw new DuplicateRequestException("Duplicate or missing idempotency scope");
        }
        ListenerState state = listenerService.resetListenerAndEvictCache(fromBlock);
        return Map.of(
                "message", "listener reset success",
                "lastProcessedBlock", state.getLastProcessedBlock(),
                "lastSafeBlock", state.getLastSafeBlock(),
                "lastWsSeenBlock", state.getLastWsSeenBlock(),
                "updatedAt", state.getUpdatedAt().toString()
        );
    }
}
