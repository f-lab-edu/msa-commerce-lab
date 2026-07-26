package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.mapstruct.Mapper;

import com.msa.commerce.monolith.product.domain.ProductCategory;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    default ProductCategory toDomain(ProductCategoryJpaEntity entity) {
        return ProductCategory.reconstitute(entity.getId(), entity.getParentId(), entity.getName(),
            entity.getDescription(), entity.getSlug(), entity.getDisplayOrder(),
            Boolean.TRUE.equals(entity.getIsActive()), Boolean.TRUE.equals(entity.getIsFeatured()),
            entity.getImageUrl(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

}
