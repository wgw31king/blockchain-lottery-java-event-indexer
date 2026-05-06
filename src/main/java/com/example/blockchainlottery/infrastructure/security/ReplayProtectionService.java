package com.example.blockchainlottery.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReplayProtectionService {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String PREFIX = "idem:";

    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ConcurrentHashMap<String, Instant> memoryFallback = new ConcurrentHashMap<>();

    public ReplayProtectionService(ObjectProvider<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @return true if this idempotency key is accepted (first use); false if replayed.
     */
    public boolean registerIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        StringRedisTemplate redis = redisTemplate.getIfAvailable();
        if (redis != null) {
            Boolean ok = redis.opsForValue().setIfAbsent(PREFIX + key, "1", IDEMPOTENCY_TTL);
            return Boolean.TRUE.equals(ok);
        }
        purgeExpiredMemory();
        Instant now = Instant.now();
        return memoryFallback.putIfAbsent(key, now) == null;
    }

    private void purgeExpiredMemory() {
        Instant cutoff = Instant.now().minus(IDEMPOTENCY_TTL);
        Iterator<Map.Entry<String, Instant>> it = memoryFallback.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Instant> e = it.next();
            if (e.getValue().isBefore(cutoff)) {
                it.remove();
            }
        }
    }
}
