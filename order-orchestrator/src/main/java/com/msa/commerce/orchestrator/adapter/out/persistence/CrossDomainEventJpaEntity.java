package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.orchestrator.domain.crossdomain.CrossDomainEvent;
import com.msa.commerce.orchestrator.domain.crossdomain.PublishingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cross_domain_events", indexes = {
    @Index(name = "idx_cross_domain_events_occurred_at", columnList = "occurred_at"),
    @Index(name = "idx_cross_domain_events_status", columnList = "publishing_status, retry_count"),
    @Index(name = "idx_cross_domain_events_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_cross_domain_events_type", columnList = "event_type, source_domain")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CrossDomainEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_uuid", length = 36, unique = true, nullable = false)
    private String eventUuid;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "source_domain", nullable = false, length = 50)
    private String sourceDomain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_domains", nullable = false, columnDefinition = "JSON")
    private String targetDomainsJson;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "entity_uuid", length = 36)
    private String entityUuid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false, columnDefinition = "JSON")
    private String eventData;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "kafka_topic", nullable = false, length = 100)
    private String kafkaTopic;

    @Column(name = "kafka_partition")
    private Integer kafkaPartition;

    @Column(name = "kafka_offset")
    private Long kafkaOffset;

    @Enumerated(EnumType.STRING)
    @Column(name = "publishing_status", nullable = false, length = 20)
    private PublishingStatus publishingStatus;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateFromDomain(CrossDomainEvent domain) {
        this.publishingStatus = domain.getPublishingStatus();
        this.retryCount = domain.getRetryCount();
        this.errorMessage = domain.getErrorMessage();
        this.kafkaPartition = domain.getKafkaPartition();
        this.kafkaOffset = domain.getKafkaOffset();
        this.publishedAt = domain.getPublishedAt();
    }

}
