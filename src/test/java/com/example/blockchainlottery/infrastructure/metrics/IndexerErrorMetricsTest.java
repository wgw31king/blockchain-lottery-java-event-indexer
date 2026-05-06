package com.example.blockchainlottery.infrastructure.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class IndexerErrorMetricsTest {

    @Test
    void incrementsCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        IndexerErrorMetrics m = new IndexerErrorMetrics(registry);
        m.increment("poll");
        assertEquals(1.0, registry.get("lottery_indexer_errors_total").tags("stage", "poll").counter().count());
    }
}
