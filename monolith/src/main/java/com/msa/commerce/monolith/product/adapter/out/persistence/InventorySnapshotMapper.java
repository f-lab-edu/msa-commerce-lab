package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.monolith.product.domain.InventorySnapshot;

@Mapper(componentModel = "spring")
public interface InventorySnapshotMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "variantId", expression = "java(entity.getVariant() != null ? entity.getVariant().getId() : null)")
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

    // updateEntity, 검증 로직은 별도 서비스/유틸로 분리 필요
}
