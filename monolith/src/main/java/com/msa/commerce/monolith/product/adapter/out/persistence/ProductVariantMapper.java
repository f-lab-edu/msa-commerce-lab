package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.mapstruct.Mapper;

import com.msa.commerce.monolith.product.domain.ProductVariant;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    default ProductVariant toDomain(ProductVariantJpaEntity entity) {
        return ProductVariant.reconstitute(entity.getId(), entity.getProductId(), entity.getVariantSku(),
            entity.getName(), entity.getPriceAdjustment(), entity.getStatus(), entity.getIsDefault(),
            entity.getOptions(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    default ProductVariantJpaEntity toEntity(ProductVariant variant) {
        return ProductVariantJpaEntity.builder()
            .id(variant.getId())
            .productId(variant.getProductId())
            .variantSku(variant.getVariantSku())
            .name(variant.getName())
            .priceAdjustment(variant.getPriceAdjustment())
            .status(variant.getStatus())
            .isDefault(variant.getIsDefault())
            .options(variant.getOptions())
            .build();
    }

}
