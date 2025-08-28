package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

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

    private final String productAttributes;

    private final ProductStatus status;

    private final String visibility;

    private final String taxClass;

    private final String metaTitle;

    private final String metaDescription;

    private final String searchKeywords;

    private final Boolean isFeatured;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final Integer availableQuantity;

    private final Integer reservedQuantity;

    private final Integer totalQuantity;

    private final Integer lowStockThreshold;

    private final Boolean isTrackingEnabled;

    private final Boolean isBackorderAllowed;

    private final Integer minOrderQuantity;

    private final Integer maxOrderQuantity;

    private final Integer reorderPoint;

    private final Integer reorderQuantity;

    private final String locationCode;

    private final Long versionNumber;

}

