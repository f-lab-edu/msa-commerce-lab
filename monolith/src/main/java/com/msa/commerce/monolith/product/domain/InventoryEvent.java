package com.msa.commerce.monolith.product.domain;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryEvent {

    private final Long id;

    private final InventoryEventType eventType;

    private final String aggregateId;

    private final Long aggregateVersion;

    private final Long productId;

    private final Long variantId;

    private final String locationCode;

    private final int quantityChange;

    private final int quantityBefore;

    private final int quantityAfter;

    private final String changeReason;

    private final String referenceType;

    private final String referenceId;

    private final String eventData;

    private final String correlationId;

    private final LocalDateTime occurredAt;

    public boolean isStockIncrease() {
        return quantityChange > 0;
    }

    public boolean isStockDecrease() {
        return quantityChange < 0;
    }

    public boolean isReservationEvent() {
        return eventType == InventoryEventType.STOCK_RESERVATION ||
            eventType == InventoryEventType.STOCK_RESERVATION_RELEASE ||
            eventType == InventoryEventType.STOCK_RESERVATION_CONFIRM;
    }

    public boolean isStockMovementEvent() {
        return eventType == InventoryEventType.STOCK_IN ||
            eventType == InventoryEventType.STOCK_OUT;
    }

    public boolean isAdjustmentEvent() {
        return eventType == InventoryEventType.STOCK_ADJUSTMENT;
    }

    public int getAbsoluteQuantityChange() {
        return Math.abs(quantityChange);
    }

    public String getEventDescription() {
        return switch (eventType) {
            case STOCK_IN -> String.format("재고 입고 +%d", getAbsoluteQuantityChange());
            case STOCK_OUT -> String.format("재고 출고 -%d", getAbsoluteQuantityChange());
            case STOCK_RESERVATION -> String.format("재고 예약 %d", getAbsoluteQuantityChange());
            case STOCK_RESERVATION_RELEASE -> String.format("예약 해제 %d", getAbsoluteQuantityChange());
            case STOCK_RESERVATION_CONFIRM -> String.format("예약 확정 %d", getAbsoluteQuantityChange());
            case STOCK_ADJUSTMENT -> String.format("재고 조정 %+d", quantityChange);
            case STOCK_TRANSFER -> String.format("재고 이동 %+d", quantityChange);
            case STOCK_DAMAGE -> String.format("재고 손상 -%d", getAbsoluteQuantityChange());
            case STOCK_LOST -> String.format("재고 분실 -%d", getAbsoluteQuantityChange());
            case STOCK_COUNT -> String.format("재고 실사 %+d", quantityChange);
        };
    }

    public boolean validateQuantityConsistency() {
        return quantityBefore + quantityChange == quantityAfter;
    }

    public InventoryEvent withCorrelationId(String newCorrelationId) {
        return InventoryEvent.builder()
            .id(this.id)
            .eventType(this.eventType)
            .aggregateId(this.aggregateId)
            .aggregateVersion(this.aggregateVersion)
            .productId(this.productId)
            .variantId(this.variantId)
            .locationCode(this.locationCode)
            .quantityChange(this.quantityChange)
            .quantityBefore(this.quantityBefore)
            .quantityAfter(this.quantityAfter)
            .changeReason(this.changeReason)
            .referenceType(this.referenceType)
            .referenceId(this.referenceId)
            .eventData(this.eventData)
            .correlationId(newCorrelationId)
            .occurredAt(this.occurredAt)
            .build();
    }

    public Map<String, Object> toEventSummary() {
        return Map.of(
            "eventType", eventType.name(),
            "productId", productId,
            "variantId", variantId != null ? variantId : "N/A",
            "locationCode", locationCode,
            "quantityChange", quantityChange,
            "quantityBefore", quantityBefore,
            "quantityAfter", quantityAfter,
            "occurredAt", occurredAt,
            "description", getEventDescription()
        );
    }

    public static class InventoryEventBuilder {

        public InventoryEventBuilder validateQuantityChange() {
            if (this.quantityBefore + this.quantityChange != this.quantityAfter) {
                throw new IllegalArgumentException(
                    String.format("Quantity consistency validation failed: %d + %d != %d",
                        this.quantityBefore, this.quantityChange, this.quantityAfter));
            }
            return this;
        }

        public InventoryEventBuilder withCurrentTimestamp() {
            this.occurredAt = LocalDateTime.now();
            return this;
        }

    }

}
