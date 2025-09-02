package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.domain.Product;

@Component
public class ProductResponseMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .shortDescription(product.getShortDescription())
            .description(product.getDescription())
            .categoryId(product.getCategoryId())
            .brand(product.getBrand())
            .productType(product.getProductType())
            .status(product.getStatus())
            .basePrice(product.getBasePrice())
            .salePrice(product.getSalePrice())
            .currency(product.getCurrency())
            .weightGrams(product.getWeightGrams())
            .requiresShipping(product.getRequiresShipping())
            .isTaxable(product.getIsTaxable())
            .isFeatured(product.getIsFeatured())
            .slug(product.getSlug())
            .searchTags(product.getSearchTags())
            .primaryImageUrl(product.getPrimaryImageUrl())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .version(product.getVersion())
            .build();
    }

    public ProductSearchResponse toSearchResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSearchResponse.builder()
            .id(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .shortDescription(product.getShortDescription())
            .description(product.getDescription())
            .categoryId(product.getCategoryId())
            .brand(product.getBrand())
            .productType(product.getProductType())
            .status(product.getStatus())
            .basePrice(product.getBasePrice())
            .salePrice(product.getSalePrice())
            .currency(product.getCurrency())
            .weightGrams(product.getWeightGrams())
            .requiresShipping(product.getRequiresShipping())
            .isTaxable(product.getIsTaxable())
            .isFeatured(product.getIsFeatured())
            .slug(product.getSlug())
            .searchTags(product.getSearchTags())
            .primaryImageUrl(product.getPrimaryImageUrl())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .version(product.getVersion())
            .viewCount(0L)
            .build();
    }

}
