package com.msa.commerce.monolith.product.application.port.in;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductUpdateCommand {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private final Long productId;

    @Positive(message = "Category ID must be positive")
    private final Long categoryId;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private final String sku;

    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private final String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private final String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private final String shortDescription;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private final String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private final String model;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private final BigDecimal price;

    private final BigDecimal comparePrice;

    private final BigDecimal costPrice;

    private final BigDecimal weight;

    private final String productAttributes;

    private final String visibility;

    private final String taxClass;

    private final String metaTitle;

    private final String metaDescription;

    private final String searchKeywords;

    private final Boolean isFeatured;

    private final Integer initialStock;

    private final Integer lowStockThreshold;

    private final Boolean isTrackingEnabled;

    private final Boolean isBackorderAllowed;

    private final Integer minOrderQuantity;

    private final Integer maxOrderQuantity;

    private final Integer reorderPoint;

    private final Integer reorderQuantity;

    private final String locationCode;

}

