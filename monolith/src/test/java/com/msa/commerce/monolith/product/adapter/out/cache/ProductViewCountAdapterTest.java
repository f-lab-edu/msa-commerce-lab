package com.msa.commerce.monolith.product.adapter.out.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductViewCountAdapter 테스트")
class ProductViewCountAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private ProductViewCountAdapter adapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        adapter = new ProductViewCountAdapter(redisTemplate);
    }

    @Test
    @DisplayName("조회수 증가 - 첫 번째 조회")
    void incrementViewCount_FirstView_SetsTTL() {
        // Given
        Long productId = 1L;
        String expectedKey = "product:view:1";
        when(valueOperations.increment(expectedKey, 1)).thenReturn(1L);

        // When
        adapter.incrementViewCount(productId);

        // Then
        verify(valueOperations).increment(expectedKey, 1);
        verify(redisTemplate).expire(expectedKey, 24, TimeUnit.HOURS);
    }

    @Test
    @DisplayName("조회수 증가 - 두 번째 이후 조회")
    void incrementViewCount_SubsequentView_DoesNotSetTTL() {
        // Given
        Long productId = 1L;
        String expectedKey = "product:view:1";
        when(valueOperations.increment(expectedKey, 1)).thenReturn(5L);

        // When
        adapter.incrementViewCount(productId);

        // Then
        verify(valueOperations).increment(expectedKey, 1);
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("조회수 증가 - Redis 예외 발생 시 예외를 던지지 않음")
    void incrementViewCount_RedisException_DoesNotThrow() {
        // Given
        Long productId = 1L;
        when(valueOperations.increment(anyString(), anyLong())).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        assertThatCode(() -> adapter.incrementViewCount(productId))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("조회수 조회 - 값이 존재하는 경우")
    void getViewCount_ValueExists_ReturnsCount() {
        // Given
        Long productId = 1L;
        String expectedKey = "product:view:1";
        when(valueOperations.get(expectedKey)).thenReturn(10);

        // When
        Long viewCount = adapter.getViewCount(productId);

        // Then
        assertThat(viewCount).isEqualTo(10L);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("조회수 조회 - 값이 존재하지 않는 경우")
    void getViewCount_ValueNotExists_ReturnsZero() {
        // Given
        Long productId = 1L;
        String expectedKey = "product:view:1";
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // When
        Long viewCount = adapter.getViewCount(productId);

        // Then
        assertThat(viewCount).isEqualTo(0L);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("조회수 조회 - String 타입 반환 시 Long으로 변환")
    void getViewCount_StringValue_ConvertsToLong() {
        // Given
        Long productId = 1L;
        String expectedKey = "product:view:1";
        when(valueOperations.get(expectedKey)).thenReturn("15");

        // When
        Long viewCount = adapter.getViewCount(productId);

        // Then
        assertThat(viewCount).isEqualTo(15L);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("조회수 조회 - Redis 예외 발생 시 0 반환")
    void getViewCount_RedisException_ReturnsZero() {
        // Given
        Long productId = 1L;
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // When
        Long viewCount = adapter.getViewCount(productId);

        // Then
        assertThat(viewCount).isEqualTo(0L);
    }
}
