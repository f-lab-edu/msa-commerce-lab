package com.msa.commerce.orchestrator.application.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    private static final Duration IDEMPOTENCY_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isProcessed(String eventId) {
        String key = IDEMPOTENCY_KEY_PREFIX + eventId;
        return redisTemplate.hasKey(key);
    }

    public void markAsProcessed(String eventId) {
        String key = IDEMPOTENCY_KEY_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, "processed", IDEMPOTENCY_TTL);
    }

}
