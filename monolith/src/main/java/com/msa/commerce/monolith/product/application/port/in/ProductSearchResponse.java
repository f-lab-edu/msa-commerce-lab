package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSearchResponse {

    private final Long id;
    private final Long categoryId;
    private final String sku;
    private final String name;
    private final String description;
    private final String shortDescription;
    private final String brand;
    private final String model;
    private final BigDecimal price;
    private final BigDecimal comparePrice;
    private final BigDecimal costPrice;
    private final BigDecimal weight;
    private final ProductStatus status;
    private final String visibility;
    private final Boolean isFeatured;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Long viewCount;
}