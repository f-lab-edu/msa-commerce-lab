package com.msa.commerce.orchestrator.domain.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import com.msa.commerce.common.util.UuidGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
@ToString
public class OutboxEvent {

    private UUID id;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    private String topic;

    private String payload;

    private String correlationId;

    private OutboxStatus status;

    private Integer retryCount;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private LocalDateTime lastAttemptAt;

    private Long version;

    @Builder
    public OutboxEvent(UUID id, String aggregateType, String aggregateId, String eventType,
        String topic, String payload, String correlationId, OutboxStatus status,
        Integer retryCount, String errorMessage, LocalDateTime createdAt,
        LocalDateTime publishedAt, LocalDateTime lastAttemptAt, Long version) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.correlationId = correlationId;
        this.status = status;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
        this.lastAttemptAt = lastAttemptAt;
        this.version = version;
    }

    public static OutboxEvent create(String aggregateType, String aggregateId,
        String eventType, String topic, String payload, String correlationId) {
        validateCreationParameters(aggregateType, aggregateId, eventType, topic, payload);

        return OutboxEvent.builder()
            .id(UuidGenerator.generate())
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .eventType(eventType)
            .topic(topic)
            .payload(payload)
            .correlationId(correlationId)
            .status(OutboxStatus.PENDING)
            .retryCount(0)
            .createdAt(LocalDateTime.now())
            .version(1L)
            .build();
    }

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public boolean canRetry(int maxRetries) {
        return this.status == OutboxStatus.PENDING && this.retryCount < maxRetries;
    }

    private static void validateCreationParameters(String aggregateType, String aggregateId,
        String eventType, String topic, String payload) {
        if (aggregateType == null || aggregateType.trim().isEmpty()) {
            throw new IllegalArgumentException("Aggregate type cannot be null or empty");
        }
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Aggregate ID cannot be null or empty");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be null or empty");
        }
    }

}
