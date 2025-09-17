package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.msa.commerce.monolith.product.domain.InventoryEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "inventory_events",
    indexes = {
        @Index(name = "idx_inventory_events_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_inventory_events_product", columnList = "product_id, occurred_at"),
        @Index(name = "idx_inventory_events_aggregate", columnList = "aggregate_id, aggregate_version"),
        @Index(name = "idx_inventory_events_product_time", columnList = "product_id, occurred_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_agg", columnNames = {"aggregate_id", "aggregate_version"}),
        @UniqueConstraint(name = "uk_inventory_corr", columnNames = {"correlation_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50, nullable = false)
    private InventoryEventType eventType;

    @Column(name = "aggregate_id", length = 255, nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_version", nullable = false)
    private Long aggregateVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductJpaEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariantJpaEntity variant;

    @Column(name = "location_code", length = 50, nullable = false)
    private String locationCode = "MAIN";

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "change_reason", length = 100)
    private String changeReason;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Builder
    public InventoryEventJpaEntity(InventoryEventType eventType, String aggregateId,
        Long aggregateVersion, ProductJpaEntity product, ProductVariantJpaEntity variant,
        String locationCode, Integer quantityChange, Integer quantityBefore,
        Integer quantityAfter, String changeReason, String referenceType,
        String referenceId, String eventData, String correlationId) {
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
        this.product = product;
        this.variant = variant;
        this.locationCode = locationCode != null ? locationCode : "MAIN";
        this.quantityChange = quantityChange;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.changeReason = changeReason;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.eventData = eventData;
        this.correlationId = correlationId;

        validateQuantityConsistency();
    }

    private void validateQuantityConsistency() {
        if (quantityAfter == null || quantityBefore == null || quantityChange == null) {
            throw new IllegalArgumentException("수량 정보는 null일 수 없습니다.");
        }

        if (!quantityAfter.equals(quantityBefore + quantityChange)) {
            throw new IllegalArgumentException(
                String.format("수량 일관성 오류: before=%d, change=%d, after=%d",
                    quantityBefore, quantityChange, quantityAfter)
            );
        }
    }

    public boolean isPositiveChange() {
        return quantityChange > 0;
    }

    public boolean isNegativeChange() {
        return quantityChange < 0;
    }

    public boolean hasReference() {
        return referenceType != null && referenceId != null;
    }

}
