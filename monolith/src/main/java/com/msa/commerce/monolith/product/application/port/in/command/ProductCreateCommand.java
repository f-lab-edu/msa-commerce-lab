package com.msa.commerce.monolith.product.application.port.in.command;

import java.math.BigDecimal;

import com.msa.commerce.monolith.product.domain.ProductType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCreateCommand {

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private final String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private final String name;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private final String shortDescription;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private final String description;

    private final Long categoryId;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private final String brand;

    private final ProductType productType;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    private final BigDecimal basePrice;

    private final BigDecimal salePrice;

    @Size(max = 3, message = "Currency must be 3 characters")
    private final String currency;

    private final Integer weightGrams;

    private final Boolean requiresShipping;

    private final Boolean isTaxable;

    private final Boolean isFeatured;

    @NotBlank(message = "Slug is required")
    @Size(max = 300, message = "Slug must not exceed 300 characters")
    private final String slug;

    private final String searchTags;

    @Size(max = 500, message = "Primary image URL must not exceed 500 characters")
    private final String primaryImageUrl;

    // Inventory fields
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
