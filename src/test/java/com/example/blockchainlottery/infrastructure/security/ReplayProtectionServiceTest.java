package com.example.blockchainlottery.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

class ReplayProtectionServiceTest {

    @Test
    void memoryFallbackAcceptsOnce() {
        @SuppressWarnings("unchecked")
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        ReplayProtectionService svc = new ReplayProtectionService(provider);
        assertTrue(svc.registerIdempotencyKey("k1"));
        assertFalse(svc.registerIdempotencyKey("k1"));
        assertFalse(svc.registerIdempotencyKey(""));
        assertFalse(svc.registerIdempotencyKey("   "));
    }
}
