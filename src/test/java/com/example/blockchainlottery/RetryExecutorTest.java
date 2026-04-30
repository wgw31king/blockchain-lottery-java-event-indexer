package com.example.blockchainlottery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.blockchainlottery.config.LotteryChainProperties;
import com.example.blockchainlottery.service.RetryExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryExecutorTest {

    @Test
    void shouldRetryTransientFailure() {
        LotteryChainProperties properties = new LotteryChainProperties();
        properties.setMaxRetryAttempts(3);
        properties.setRetryBackoffMs(1);

        RetryExecutor retryExecutor = new RetryExecutor(properties);
        AtomicInteger counter = new AtomicInteger();

        int result = retryExecutor.execute("test", () -> {
            int attempt = counter.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("connection timeout");
            }
            return attempt;
        });

        assertEquals(3, result);
    }
}
