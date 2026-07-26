package com.msa.commerce.payment.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.msa.commerce.payment.domain.outbox.OutboxEvent;
import com.msa.commerce.payment.domain.outbox.PublishingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;

    @Column(nullable = false, length = 100)
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSON")
    private String payload;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "publishing_status", nullable = false, length = 20)
    private PublishingStatus publishingStatus;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "kafka_partition")
    private Integer kafkaPartition;

    @Column(name = "kafka_offset")
    private Long kafkaOffset;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OutboxEventJpaEntity from(OutboxEvent event) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.id = event.getId();
        entity.eventId = event.getEventId();
        entity.eventType = event.getEventType();
        entity.aggregateId = event.getAggregateId();
        entity.topic = event.getTopic();
        entity.payload = event.getPayload();
        entity.correlationId = event.getCorrelationId();
        entity.maxRetries = event.getMaxRetries();
        entity.occurredAt = event.getOccurredAt();
        entity.createdAt = event.getCreatedAt();
        entity.applyPublishingState(event);
        return entity;
    }

    public void updateFrom(OutboxEvent event) {
        applyPublishingState(event);
    }

    public OutboxEvent toDomain() {
        return OutboxEvent.builder()
            .id(id)
            .eventId(eventId)
            .eventType(eventType)
            .aggregateId(aggregateId)
            .topic(topic)
            .payload(payload)
            .correlationId(correlationId)
            .publishingStatus(publishingStatus)
            .retryCount(retryCount)
            .maxRetries(maxRetries)
            .errorMessage(errorMessage)
            .kafkaPartition(kafkaPartition)
            .kafkaOffset(kafkaOffset)
            .occurredAt(occurredAt)
            .publishedAt(publishedAt)
            .createdAt(createdAt)
            .build();
    }

    private void applyPublishingState(OutboxEvent event) {
        this.publishingStatus = event.getPublishingStatus();
        this.retryCount = event.getRetryCount();
        this.errorMessage = event.getErrorMessage();
        this.kafkaPartition = event.getKafkaPartition();
        this.kafkaOffset = event.getKafkaOffset();
        this.publishedAt = event.getPublishedAt();
    }

}
