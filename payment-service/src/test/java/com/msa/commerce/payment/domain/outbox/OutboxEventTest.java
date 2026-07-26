package com.msa.commerce.payment.domain.outbox;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OutboxEvent 도메인 테스트")
class OutboxEventTest {

    @Test
    @DisplayName("생성 시 PENDING 상태로 재시도 카운터가 초기화된다")
    void createsPendingEvent() {
        OutboxEvent event = pendingEvent();

        assertThat(event.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getMaxRetries()).isEqualTo(3);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.isExhausted()).isFalse();
    }

    @Test
    @DisplayName("발행에 성공하면 파티션과 오프셋이 기록된다")
    void marksAsPublished() {
        OutboxEvent event = pendingEvent();

        event.markAsPublished(2, 100L);

        assertThat(event.getPublishingStatus()).isEqualTo(PublishingStatus.PUBLISHED);
        assertThat(event.getKafkaPartition()).isEqualTo(2);
        assertThat(event.getKafkaOffset()).isEqualTo(100L);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("실패 횟수가 한도 미만이면 PENDING 을 유지해 다음 릴레이가 다시 집어간다")
    void staysPendingUntilRetriesExhausted() {
        OutboxEvent event = pendingEvent();

        event.markAsFailed("broker unavailable");

        assertThat(event.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getErrorMessage()).isEqualTo("broker unavailable");
        assertThat(event.isExhausted()).isFalse();
    }

    @Test
    @DisplayName("실패가 한도에 도달하면 FAILED 로 떨어져 재시도 대상에서 빠진다")
    void becomesFailedWhenRetriesExhausted() {
        OutboxEvent event = pendingEvent();

        event.markAsFailed("broker unavailable");
        event.markAsFailed("broker unavailable");
        event.markAsFailed("broker unavailable");

        assertThat(event.getPublishingStatus()).isEqualTo(PublishingStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.isExhausted()).isTrue();
    }

    @Test
    @DisplayName("동일성은 eventId 로 판단한다")
    void equalsByEventId() {
        OutboxEvent event = pendingEvent();
        OutboxEvent same = OutboxEvent.builder().eventId(event.getEventId()).build();

        assertThat(event).isEqualTo(same).hasSameHashCodeAs(same);
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.pending("event-1", "PAYMENT_RESULT", "order-1", "payment.result", "{}", "corr-1");
    }

}
