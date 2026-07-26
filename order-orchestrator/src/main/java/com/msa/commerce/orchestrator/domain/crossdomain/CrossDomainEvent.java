package com.msa.commerce.orchestrator.domain.crossdomain;

import java.time.LocalDateTime;
import java.util.List;

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
public class CrossDomainEvent {

    private Long id;

    private String eventUuid;

    private EventType eventType;

    private DomainType sourceDomain;

    private List<DomainType> targetDomains;

    private EntityType entityType;

    private String entityId;

    private String entityUuid;

    private String eventData;

    private String correlationId;

    private String kafkaTopic;

    private Integer kafkaPartition;

    private Long kafkaOffset;

    private PublishingStatus publishingStatus;

    private Integer retryCount;

    private Integer maxRetries;

    private String errorMessage;

    private LocalDateTime occurredAt;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    @Builder
    public CrossDomainEvent(Long id, String eventUuid, EventType eventType, DomainType sourceDomain,
        List<DomainType> targetDomains, EntityType entityType, String entityId, String entityUuid,
        String eventData, String correlationId, String kafkaTopic, Integer kafkaPartition,
        Long kafkaOffset, PublishingStatus publishingStatus, Integer retryCount,
        Integer maxRetries, String errorMessage, LocalDateTime occurredAt,
        LocalDateTime publishedAt, LocalDateTime createdAt) {
        this.id = id;
        this.eventUuid = eventUuid;
        this.eventType = eventType;
        this.sourceDomain = sourceDomain;
        this.targetDomains = targetDomains;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityUuid = entityUuid;
        this.eventData = eventData;
        this.correlationId = correlationId;
        this.kafkaTopic = kafkaTopic;
        this.kafkaPartition = kafkaPartition;
        this.kafkaOffset = kafkaOffset;
        this.publishingStatus = publishingStatus;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.errorMessage = errorMessage;
        this.occurredAt = occurredAt;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
    }

    public static CrossDomainEvent create(EventType eventType, DomainType sourceDomain,
        List<DomainType> targetDomains, EntityType entityType, String entityId, String entityUuid,
        String eventData, String correlationId, String kafkaTopic) {

        validateCreationParameters(eventType, sourceDomain, targetDomains, entityType,
            entityId, eventData, kafkaTopic);

        return CrossDomainEvent.builder()
            .eventType(eventType)
            .sourceDomain(sourceDomain)
            .targetDomains(targetDomains)
            .entityType(entityType)
            .entityId(entityId)
            .entityUuid(entityUuid)
            .eventData(eventData)
            .correlationId(correlationId)
            .kafkaTopic(kafkaTopic)
            .publishingStatus(PublishingStatus.PENDING)
            .retryCount(0)
            .maxRetries(3)
            .occurredAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();
    }

    public void markAsPublished(Integer partition, Long offset) {
        this.publishingStatus = PublishingStatus.PUBLISHED;
        this.kafkaPartition = partition;
        this.kafkaOffset = offset;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.publishingStatus = PublishingStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.publishingStatus == PublishingStatus.PENDING
            && this.retryCount < this.maxRetries;
    }

    private static void validateCreationParameters(EventType eventType, DomainType sourceDomain,
        List<DomainType> targetDomains, EntityType entityType, String entityId, String eventData,
        String kafkaTopic) {

        CrossDomainEventValidator.builder()
            .requireNonNull(eventType, "Event type")
            .requireNonNull(sourceDomain, "Source domain")
            .requireNonEmpty(targetDomains, "Target domains")
            .requireNonNull(entityType, "Entity type")
            .requireNonEmpty(entityId, "Entity ID")
            .requireNonEmpty(eventData, "Event data")
            .requireNonEmpty(kafkaTopic, "Kafka topic")
            .validate();
    }

}
