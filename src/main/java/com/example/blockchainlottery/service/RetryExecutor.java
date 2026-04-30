package com.example.blockchainlottery.service;

import com.example.blockchainlottery.config.LotteryChainProperties;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

    private final LotteryChainProperties properties;

    public RetryExecutor(LotteryChainProperties properties) {
        this.properties = properties;
    }

    public <T> T execute(String operationName, Supplier<T> supplier) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= properties.getMaxRetryAttempts(); attempt++) {
            try {
                return supplier.get();
            } catch (RuntimeException ex) {
                lastException = ex;
                if (!isRetryable(ex)) {
                    throw ex;
                }
                if (attempt == properties.getMaxRetryAttempts()) {
                    break;
                }

                long sleepMs = properties.getRetryBackoffMs() * attempt;
                log.warn("Operation {} failed on attempt {}/{}. Retrying in {} ms", operationName, attempt,
                        properties.getMaxRetryAttempts(), sleepMs, ex);
                sleep(sleepMs);
            }
        }

        throw new IllegalStateException("Operation failed after retries: " + operationName, lastException);
    }

    private boolean isRetryable(RuntimeException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        return message.contains("timeout")
                || message.contains("connection")
                || message.contains("429")
                || message.contains("rate")
                || message.contains("temporarily")
                || message.contains("io exception");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted after " + Duration.ofMillis(millis), e);
        }
    }
}
