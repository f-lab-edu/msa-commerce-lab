package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.monolith.product.domain.InventoryEvent;

@Mapper(componentModel = "spring")
public interface InventoryEventMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "variantId", expression = "java(entity.getVariant() != null ? entity.getVariant().getId() : null)")
    InventoryEvent toDomain(InventoryEventJpaEntity entity);

    List<InventoryEvent> toDomainList(List<InventoryEventJpaEntity> entities);

    default InventoryEventJpaEntity toEntity(InventoryEvent domain, @Context ProductJpaEntity productEntity, @Context ProductVariantJpaEntity variantEntity) {
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

    // 검증 로직은 별도 서비스/유틸로 분리 필요
}
