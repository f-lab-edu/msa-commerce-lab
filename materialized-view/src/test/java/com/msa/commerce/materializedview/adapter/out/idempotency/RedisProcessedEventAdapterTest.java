package com.msa.commerce.materializedview.adapter.out.idempotency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisProcessedEventAdapter 단위 테스트")
class RedisProcessedEventAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisProcessedEventAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RedisProcessedEventAdapter(redisTemplate);
    }

    @Test
    @DisplayName("최초 처리 시 마크에 성공한다")
    void marksFirstProcessing() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("mv:processed-event:event-1"), eq("1"), any(Duration.class)))
            .thenReturn(true);

        assertThat(adapter.markProcessed("event-1")).isTrue();
    }

    @Test
    @DisplayName("이미 마크된 이벤트는 false를 반환한다")
    void rejectsDuplicate() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);

        assertThat(adapter.markProcessed("event-1")).isFalse();
    }

    @Test
    @DisplayName("unmark는 처리 마크 키를 삭제한다")
    void unmarkDeletesKey() {
        adapter.unmark("event-1");

        verify(redisTemplate).delete("mv:processed-event:event-1");
    }

}
