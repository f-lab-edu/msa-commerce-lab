package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.monolith.product.domain.InventoryEvent;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InventoryEventMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "variant.id")
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

}
