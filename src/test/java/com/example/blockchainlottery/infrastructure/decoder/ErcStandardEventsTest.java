package com.example.blockchainlottery.infrastructure.decoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErcStandardEventsTest {

    @Test
    void enablesSelectedStandards() {
        List<EventDefinition> all = ErcStandardEvents.definitions(true, true, true);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(e -> "Transfer".equals(e.getName())));
        assertTrue(all.stream().anyMatch(e -> "TransferSingle".equals(e.getName())));
        assertTrue(all.stream().anyMatch(e -> "TransferBatch".equals(e.getName())));
    }

    @Test
    void noneWhenAllDisabled() {
        assertTrue(ErcStandardEvents.definitions(false, false, false).isEmpty());
    }
}
