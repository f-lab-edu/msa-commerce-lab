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

import com.msa.commerce.monolith.product.domain.InventorySnapshot;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface InventorySnapshotMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "stockStatus", expression = "java(entity.calculateStockStatus())")
    InventorySnapshot toDomain(InventorySnapshotJpaEntity entity);

    List<InventorySnapshot> toDomainList(List<InventorySnapshotJpaEntity> entities);

    default InventorySnapshotJpaEntity toEntity(InventorySnapshot domain, @Context ProductJpaEntity productEntity, @Context ProductVariantJpaEntity variantEntity) {
        return InventorySnapshotJpaEntity.builder()
            .product(productEntity)
            .variant(variantEntity)
            .locationCode(domain.getLocationCode())
            .availableQuantity(domain.getAvailableQuantity())
            .reservedQuantity(domain.getReservedQuantity())
            .lowStockThreshold(domain.getLowStockThreshold())
            .build();
    }

}
