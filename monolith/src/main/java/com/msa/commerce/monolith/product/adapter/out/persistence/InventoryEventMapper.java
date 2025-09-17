package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.domain.InventoryEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryEventMapper {

    public InventoryEvent toDomain(InventoryEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return InventoryEvent.builder()
            .id(entity.getId())
            .eventType(entity.getEventType())
            .aggregateId(entity.getAggregateId())
            .aggregateVersion(entity.getAggregateVersion())
            .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
            .variantId(entity.getVariant() != null ? entity.getVariant().getId() : null)
            .locationCode(entity.getLocationCode())
            .quantityChange(entity.getQuantityChange())
            .quantityBefore(entity.getQuantityBefore())
            .quantityAfter(entity.getQuantityAfter())
            .changeReason(entity.getChangeReason())
            .referenceType(entity.getReferenceType())
            .referenceId(entity.getReferenceId())
            .eventData(entity.getEventData())
            .correlationId(entity.getCorrelationId())
            .occurredAt(entity.getOccurredAt())
            .build();
    }

    public InventoryEventJpaEntity toEntity(InventoryEvent domain,
        ProductJpaEntity productEntity,
        ProductVariantJpaEntity variantEntity) {
        if (domain == null) {
            return null;
        }

        return InventoryEventJpaEntity.builder()
            .eventType(domain.getEventType())
            .aggregateId(domain.getAggregateId())
            .aggregateVersion(domain.getAggregateVersion())
            .product(productEntity)
            .variant(variantEntity)
            .locationCode(domain.getLocationCode())
            .quantityChange(domain.getQuantityChange())
            .quantityBefore(domain.getQuantityBefore())
            .quantityAfter(domain.getQuantityAfter())
            .changeReason(domain.getChangeReason())
            .referenceType(domain.getReferenceType())
            .referenceId(domain.getReferenceId())
            .eventData(domain.getEventData())
            .correlationId(domain.getCorrelationId())
            .build();
    }

    public List<InventoryEvent> toDomainList(List<InventoryEventJpaEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
            .map(this::toDomain)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    public InventoryEvent toDomainWithValidation(InventoryEventJpaEntity entity) {
        InventoryEvent domain = toDomain(entity);

        if (domain != null) {
            validateInventoryEvent(domain);
        }

        return domain;
    }

    private void validateInventoryEvent(InventoryEvent event) {
        if (event.getEventType() == null) {
            throw new IllegalArgumentException("이벤트 타입은 필수입니다.");
        }

        if (event.getAggregateId() == null || event.getAggregateId().trim().isEmpty()) {
            throw new IllegalArgumentException("집계 ID는 필수입니다.");
        }

        if (event.getAggregateVersion() == null || event.getAggregateVersion() <= 0) {
            throw new IllegalArgumentException("집계 버전은 양수여야 합니다.");
        }

        if (event.getLocationCode() == null || event.getLocationCode().trim().isEmpty()) {
            throw new IllegalArgumentException("위치 코드는 필수입니다.");
        }

        if (!event.validateQuantityConsistency()) {
            throw new IllegalArgumentException("수량 일관성 검증에 실패했습니다.");
        }

        if (event.getCorrelationId() == null || event.getCorrelationId().trim().isEmpty()) {
            throw new IllegalArgumentException("상관관계 ID는 필수입니다.");
        }
    }

}
