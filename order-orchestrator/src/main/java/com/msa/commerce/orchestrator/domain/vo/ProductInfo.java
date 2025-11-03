package com.msa.commerce.orchestrator.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record ProductInfo(
    Long productId, String productName, String sku,
    String imageUrl, String description, BigDecimal price
) {

    public ProductInfo {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(productName, "Product name cannot be null");
        Objects.requireNonNull(sku, "SKU cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
    }

}
