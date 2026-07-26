package com.msa.commerce.monolith.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant {

    private Long id;

    private Long productId;

    private String variantSku;

    private String name;

    private BigDecimal priceAdjustment;

    private ProductVariantStatus status;

    private Boolean isDefault;

    private Map<String, Object> options;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public ProductVariant(Long productId, String variantSku, String name,
        BigDecimal priceAdjustment, Boolean isDefault, Map<String, Object> options) {
        validateVariant(productId, variantSku, name);

        this.productId = productId;
        this.variantSku = variantSku;
        this.name = name;
        this.priceAdjustment = priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
        this.status = ProductVariantStatus.ACTIVE;
        this.isDefault = isDefault != null ? isDefault : false;
        this.options = options != null ? Map.copyOf(options) : Map.of();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductVariant reconstitute(Long id, Long productId, String variantSku,
        String name, BigDecimal priceAdjustment, ProductVariantStatus status, Boolean isDefault,
        Map<String, Object> options, LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProductVariant variant = new ProductVariant();
        variant.id = id;
        variant.productId = productId;
        variant.variantSku = variantSku;
        variant.name = name;
        variant.priceAdjustment = priceAdjustment;
        variant.status = status;
        variant.isDefault = isDefault;
        variant.options = options != null ? Map.copyOf(options) : Map.of();
        variant.createdAt = createdAt;
        variant.updatedAt = updatedAt;
        return variant;
    }

    private static void validateVariant(Long productId, String variantSku, String name) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (variantSku == null || variantSku.isBlank()) {
            throw new IllegalArgumentException("Variant SKU is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Variant name is required.");
        }
    }

    public BigDecimal effectivePrice(BigDecimal basePrice) {
        return basePrice.add(priceAdjustment);
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = ProductVariantStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markOutOfStock() {
        this.status = ProductVariantStatus.OUT_OF_STOCK;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOrderable() {
        return status == ProductVariantStatus.ACTIVE;
    }

}
