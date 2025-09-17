package com.msa.commerce.monolith.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductVariant {

    private final Long id;

    private final Long productId;

    private final String variantSku;

    private final String name;

    private final BigDecimal priceAdjustment;

    private final ProductVariantStatus status;

    private final Boolean isDefault;

    private final Map<String, Object> options;

    private final String color;

    private final String size;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public BigDecimal calculateEffectivePrice(BigDecimal basePrice) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }

        if (priceAdjustment == null) {
            return basePrice;
        }

        return basePrice.add(priceAdjustment);
    }

    public boolean isActive() {
        return status == ProductVariantStatus.ACTIVE;
    }

    public boolean isOutOfStock() {
        return status == ProductVariantStatus.OUT_OF_STOCK;
    }

    public String getOptionValue(String optionKey) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        Object value = options.get(optionKey);
        return value != null ? value.toString() : null;
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    public ProductVariant withStatus(ProductVariantStatus newStatus) {
        return ProductVariant.builder()
            .id(this.id)
            .productId(this.productId)
            .variantSku(this.variantSku)
            .name(this.name)
            .priceAdjustment(this.priceAdjustment)
            .status(newStatus)
            .isDefault(this.isDefault)
            .options(this.options)
            .color(this.color)
            .size(this.size)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }

    public ProductVariant withPriceAdjustment(BigDecimal newPriceAdjustment) {
        return ProductVariant.builder()
            .id(this.id)
            .productId(this.productId)
            .variantSku(this.variantSku)
            .name(this.name)
            .priceAdjustment(newPriceAdjustment)
            .status(this.status)
            .isDefault(this.isDefault)
            .options(this.options)
            .color(this.color)
            .size(this.size)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }

}
