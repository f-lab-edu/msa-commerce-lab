package com.msa.commerce.monolith.product.adapter.out.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductViewCountAdapter implements ProductViewCountPort {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VIEW_COUNT_KEY_PREFIX = "product:view:";

    private static final long VIEW_COUNT_TTL_HOURS = 24;

    @Override
    @Async
    public void incrementViewCount(Long productId) {
        try {
            String key = getViewCountKey(productId);

            // Redis INCR을 사용하여 원자적 증가
            Long newCount = redisTemplate.opsForValue().increment(key, 1);

            // TTL 설정 (첫 번째 증가 시에만)
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(key, VIEW_COUNT_TTL_HOURS, TimeUnit.HOURS);
            }

            log.debug("View count incremented for product {}: {}", productId, newCount);

        } catch (Exception e) {
            log.error("Failed to increment view count for product {}", productId, e);
            // 비동기 메서드이므로 예외를 다시 던지지 않음
        }
    }

    @Override
    public Long getViewCount(Long productId) {
        try {
            String key = getViewCountKey(productId);
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                return 0L;
            }
            
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            
            return Long.parseLong(value.toString());
            
        } catch (Exception e) {
            log.error("Failed to get view count for product {}", productId, e);
            return 0L;
        }
    }

    private String getViewCountKey(Long productId) {
        return VIEW_COUNT_KEY_PREFIX + productId;
    }

}

