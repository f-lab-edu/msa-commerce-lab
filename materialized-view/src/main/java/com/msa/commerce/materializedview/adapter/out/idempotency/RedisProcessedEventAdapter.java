package com.msa.commerce.materializedview.adapter.out.idempotency;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.msa.commerce.materializedview.application.port.out.ProcessedEventPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisProcessedEventAdapter implements ProcessedEventPort {

    private static final String KEY_PREFIX = "mv:processed-event:";

    private static final Duration RETENTION = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean markProcessed(String eventId) {
        Boolean marked = redisTemplate.opsForValue()
            .setIfAbsent(KEY_PREFIX + eventId, "1", RETENTION);
        return Boolean.TRUE.equals(marked);
    }

    @Override
    public void unmark(String eventId) {
        redisTemplate.delete(KEY_PREFIX + eventId);
    }

}
