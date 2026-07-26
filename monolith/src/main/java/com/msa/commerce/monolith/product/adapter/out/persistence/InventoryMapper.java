package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.mapstruct.Mapper;

import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    default InventorySnapshot toDomain(InventorySnapshotJpaEntity entity) {
        return InventorySnapshot.reconstitute(entity.getId(), entity.getProductId(), entity.getVariantId(),
            entity.getLocationCode(), entity.getAvailableQuantity(), entity.getReservedQuantity(),
            entity.getLowStockThreshold(), entity.getLastUpdatedAt(), entity.getVersion());
    }

    default InventorySnapshotJpaEntity toEntity(InventorySnapshot snapshot) {
        return InventorySnapshotJpaEntity.builder()
            .id(snapshot.getId())
            .productId(snapshot.getProductId())
            .variantId(snapshot.getVariantId())
            .locationCode(snapshot.getLocationCode())
            .availableQuantity(snapshot.getAvailableQuantity())
            .reservedQuantity(snapshot.getReservedQuantity())
            .lowStockThreshold(snapshot.getLowStockThreshold())
            .lastUpdatedAt(snapshot.getLastUpdatedAt())
            .version(snapshot.getVersion())
            .build();
    }

    default InventoryEvent toDomain(InventoryEventJpaEntity entity) {
        return InventoryEvent.reconstitute(entity.getId(), entity.getEventType(), entity.getAggregateId(),
            entity.getAggregateVersion(), entity.getProductId(), entity.getVariantId(),
            entity.getLocationCode(), entity.getQuantityChange(), entity.getQuantityBefore(),
            entity.getQuantityAfter(), entity.getChangeReason(), entity.getReferenceType(),
            entity.getReferenceId(), entity.getCorrelationId(), entity.getOccurredAt());
    }

    default InventoryEventJpaEntity toEntity(InventoryEvent event) {
        return InventoryEventJpaEntity.builder()
            .id(event.getId())
            .eventType(event.getEventType())
            .aggregateId(event.getAggregateId())
            .aggregateVersion(event.getAggregateVersion())
            .productId(event.getProductId())
            .variantId(event.getVariantId())
            .locationCode(event.getLocationCode())
            .quantityChange(event.getQuantityChange())
            .quantityBefore(event.getQuantityBefore())
            .quantityAfter(event.getQuantityAfter())
            .changeReason(event.getChangeReason())
            .referenceType(event.getReferenceType())
            .referenceId(event.getReferenceId())
            .correlationId(event.getCorrelationId())
            .occurredAt(event.getOccurredAt())
            .build();
    }

}
