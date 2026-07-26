package com.msa.commerce.payment.domain.outbox;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "eventId")
public class OutboxEvent {

    private static final int DEFAULT_MAX_RETRIES = 3;

    private Long id;

    private String eventId;

    private String eventType;

    private String aggregateId;

    private String topic;

    private String payload;

    private String correlationId;

    private PublishingStatus publishingStatus;

    private Integer retryCount;

    private Integer maxRetries;

    private String errorMessage;

    private Integer kafkaPartition;

    private Long kafkaOffset;

    private LocalDateTime occurredAt;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    @Builder
    public OutboxEvent(Long id, String eventId, String eventType, String aggregateId, String topic, String payload,
        String correlationId, PublishingStatus publishingStatus, Integer retryCount, Integer maxRetries,
        String errorMessage, Integer kafkaPartition, Long kafkaOffset, LocalDateTime occurredAt,
        LocalDateTime publishedAt, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.payload = payload;
        this.correlationId = correlationId;
        this.publishingStatus = publishingStatus;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.errorMessage = errorMessage;
        this.kafkaPartition = kafkaPartition;
        this.kafkaOffset = kafkaOffset;
        this.occurredAt = occurredAt;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
    }

    public static OutboxEvent pending(String eventId, String eventType, String aggregateId, String topic,
        String payload, String correlationId) {
        LocalDateTime now = LocalDateTime.now();

        return OutboxEvent.builder()
            .eventId(eventId)
            .eventType(eventType)
            .aggregateId(aggregateId)
            .topic(topic)
            .payload(payload)
            .correlationId(correlationId)
            .publishingStatus(PublishingStatus.PENDING)
            .retryCount(0)
            .maxRetries(DEFAULT_MAX_RETRIES)
            .occurredAt(now)
            .createdAt(now)
            .build();
    }

    public void markAsPublished(Integer partition, Long offset) {
        this.publishingStatus = PublishingStatus.PUBLISHED;
        this.kafkaPartition = partition;
        this.kafkaOffset = offset;
        this.publishedAt = LocalDateTime.now();
    }

    // 재시도 한도를 넘기면 FAILED 로 떨어뜨려 릴레이가 더 이상 집어가지 않게 한다.
    public void markAsFailed(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;

        if (retryCount >= maxRetries) {
            this.publishingStatus = PublishingStatus.FAILED;
        }
    }

    public boolean isExhausted() {
        return publishingStatus == PublishingStatus.FAILED;
    }

}
