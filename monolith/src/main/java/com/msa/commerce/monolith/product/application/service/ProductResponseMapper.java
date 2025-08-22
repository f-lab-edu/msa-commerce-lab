package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.domain.Product;

@Component
public class ProductResponseMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
            .id(product.getId())
            .categoryId(product.getCategoryId())
            .sku(product.getSku())
            .name(product.getName())
            .description(product.getDescription())
            .shortDescription(product.getShortDescription())
            .brand(product.getBrand())
            .model(product.getModel())
            .price(product.getPrice())
            .comparePrice(product.getComparePrice())
            .costPrice(product.getCostPrice())
            .weight(product.getWeight())
            .productAttributes(product.getProductAttributes())
            .status(product.getStatus())
            .visibility(product.getVisibility())
            .taxClass(product.getTaxClass())
            .metaTitle(product.getMetaTitle())
            .metaDescription(product.getMetaDescription())
            .searchKeywords(product.getSearchKeywords())
            .isFeatured(product.getIsFeatured())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

}
