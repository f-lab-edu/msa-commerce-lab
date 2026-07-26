package com.msa.commerce.payment.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

@DisplayName("OutboxEventJpaEntity 변환 테스트")
class OutboxEventJpaEntityTest {

    private static final String EVENT_ID = "event-1";

    @Test
    @DisplayName("도메인 → 엔티티 → 도메인 왕복 변환에서 값이 보존된다")
    void roundTripConversion() {
        OutboxEvent event = pendingEvent();

        OutboxEvent restored = OutboxEventJpaEntity.from(event).toDomain();

        assertThat(restored.getEventId()).isEqualTo(EVENT_ID);
        assertThat(restored.getEventType()).isEqualTo("PAYMENT_RESULT");
        assertThat(restored.getAggregateId()).isEqualTo("order-1");
        assertThat(restored.getTopic()).isEqualTo("payment.result");
        assertThat(restored.getPayload()).isEqualTo("{}");
        assertThat(restored.getCorrelationId()).isEqualTo("corr-1");
        assertThat(restored.getPublishingStatus()).isEqualTo(PublishingStatus.PENDING);
        assertThat(restored.getRetryCount()).isZero();
        assertThat(restored.getMaxRetries()).isEqualTo(3);
        assertThat(restored.getOccurredAt()).isEqualTo(event.getOccurredAt());
        assertThat(restored.getCreatedAt()).isEqualTo(event.getCreatedAt());
    }

    @Test
    @DisplayName("updateFrom 은 발행 상태만 갱신하고 식별자와 payload 는 건드리지 않는다")
    void updateFromOnlyTouchesPublishingState() {
        OutboxEvent event = pendingEvent();
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.from(event);

        event.markAsPublished(2, 100L);
        entity.updateFrom(event);

        assertThat(entity.getEventId()).isEqualTo(EVENT_ID);
        assertThat(entity.getPayload()).isEqualTo("{}");
        assertThat(entity.getOccurredAt()).isEqualTo(event.getOccurredAt());
        assertThat(entity.getPublishingStatus()).isEqualTo(PublishingStatus.PUBLISHED);
        assertThat(entity.getKafkaPartition()).isEqualTo(2);
        assertThat(entity.getKafkaOffset()).isEqualTo(100L);
        assertThat(entity.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("실패 정보도 엔티티에 반영된다")
    void carriesFailureState() {
        OutboxEvent event = pendingEvent();
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.from(event);

        event.markAsFailed("broker unavailable");
        entity.updateFrom(event);

        assertThat(entity.getRetryCount()).isEqualTo(1);
        assertThat(entity.getErrorMessage()).isEqualTo("broker unavailable");
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.pending(EVENT_ID, "PAYMENT_RESULT", "order-1", "payment.result", "{}", "corr-1");
    }

}
