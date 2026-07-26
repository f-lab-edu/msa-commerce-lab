package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryEvent {

    private Long id;

    private InventoryEventType eventType;

    private String aggregateId;

    private Long aggregateVersion;

    private Long productId;

    private Long variantId;

    private String locationCode;

    private int quantityChange;

    private int quantityBefore;

    private int quantityAfter;

    private String changeReason;

    private String referenceType;

    private String referenceId;

    private String correlationId;

    private LocalDateTime occurredAt;

    public static InventoryEvent record(InventoryEventType eventType, InventorySnapshot snapshot,
        int quantityBefore, ChangeContext context) {
        InventoryEvent event = new InventoryEvent();
        event.eventType = eventType;
        event.aggregateId = aggregateIdOf(snapshot);
        event.aggregateVersion = snapshot.getVersion();
        event.productId = snapshot.getProductId();
        event.variantId = snapshot.getVariantId();
        event.locationCode = snapshot.getLocationCode();
        event.quantityBefore = quantityBefore;
        event.quantityAfter = snapshot.getAvailableQuantity();
        event.quantityChange = event.quantityAfter - quantityBefore;
        event.changeReason = context.reason();
        event.referenceType = context.referenceType();
        event.referenceId = context.referenceId();
        event.correlationId = UUID.randomUUID().toString();
        event.occurredAt = LocalDateTime.now();
        return event;
    }

    public static InventoryEvent reconstitute(Long id, InventoryEventType eventType, String aggregateId,
        Long aggregateVersion, Long productId, Long variantId, String locationCode, int quantityChange,
        int quantityBefore, int quantityAfter, String changeReason, String referenceType,
        String referenceId, String correlationId, LocalDateTime occurredAt) {
        InventoryEvent event = new InventoryEvent();
        event.id = id;
        event.eventType = eventType;
        event.aggregateId = aggregateId;
        event.aggregateVersion = aggregateVersion;
        event.productId = productId;
        event.variantId = variantId;
        event.locationCode = locationCode;
        event.quantityChange = quantityChange;
        event.quantityBefore = quantityBefore;
        event.quantityAfter = quantityAfter;
        event.changeReason = changeReason;
        event.referenceType = referenceType;
        event.referenceId = referenceId;
        event.correlationId = correlationId;
        event.occurredAt = occurredAt;
        return event;
    }

    private static String aggregateIdOf(InventorySnapshot snapshot) {
        String variantPart = snapshot.getVariantId() != null ? snapshot.getVariantId().toString() : "NA";
        return "PRODUCT_%d_V%s_%s".formatted(snapshot.getProductId(), variantPart, snapshot.getLocationCode());
    }

    @Builder
    public record ChangeContext(String reason, String referenceType, String referenceId) {

        public static ChangeContext of(String reason) {
            return new ChangeContext(reason, null, null);
        }

    }

}
