package com.example.blockchainlottery.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class IndexerErrorMetrics {

    private final MeterRegistry registry;

    public IndexerErrorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void increment(String stage) {
        Counter.builder("lottery_indexer_errors_total")
                .tag("stage", stage)
                .register(registry)
                .increment();
    }
}
