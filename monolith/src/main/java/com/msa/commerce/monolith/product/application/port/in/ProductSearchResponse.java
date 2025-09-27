package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSearchResponse {

    private final Long id;

    private final String sku;

    private final String name;

    private final String shortDescription;

    private final String description;

    private final Long categoryId;

    private final String brand;

    private final ProductType productType;

    private final ProductStatus status;

    private final BigDecimal basePrice;

    private final BigDecimal salePrice;

    private final String currency;

    private final Integer weightGrams;

    private final Boolean requiresShipping;

    private final Boolean isTaxable;

    private final Boolean isFeatured;

    private final String slug;

    private final String searchTags;

    private final String primaryImageUrl;

    private final Integer minOrderQuantity;

    private final Integer maxOrderQuantity;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final Long version;

    private final Long viewCount;

}
