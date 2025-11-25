package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.orchestrator.domain.outbox.OutboxEvent;
import com.msa.commerce.orchestrator.domain.outbox.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
    @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OutboxEventJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Version
    @Column(name = "version")
    private Long version;

    public static OutboxEventJpaEntity fromDomain(OutboxEvent domain) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.id = domain.getId();
        entity.aggregateType = domain.getAggregateType();
        entity.aggregateId = domain.getAggregateId();
        entity.eventType = domain.getEventType();
        entity.topic = domain.getTopic();
        entity.payload = domain.getPayload();
        entity.correlationId = domain.getCorrelationId();
        entity.status = domain.getStatus();
        entity.retryCount = domain.getRetryCount();
        entity.errorMessage = domain.getErrorMessage();
        entity.createdAt = domain.getCreatedAt();
        entity.publishedAt = domain.getPublishedAt();
        entity.lastAttemptAt = domain.getLastAttemptAt();
        entity.version = domain.getVersion();
        return entity;
    }

    public OutboxEvent toDomain() {
        return OutboxEvent.builder()
            .id(id)
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .eventType(eventType)
            .topic(topic)
            .payload(payload)
            .correlationId(correlationId)
            .status(status)
            .retryCount(retryCount)
            .errorMessage(errorMessage)
            .createdAt(createdAt)
            .publishedAt(publishedAt)
            .lastAttemptAt(lastAttemptAt)
            .version(version)
            .build();
    }

    public void updateFromDomain(OutboxEvent domain) {
        this.status = domain.getStatus();
        this.retryCount = domain.getRetryCount();
        this.errorMessage = domain.getErrorMessage();
        this.publishedAt = domain.getPublishedAt();
        this.lastAttemptAt = domain.getLastAttemptAt();
    }

}
