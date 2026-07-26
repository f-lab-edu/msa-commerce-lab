package com.msa.commerce.orchestrator.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record VerifiedProduct(
    Long productId,
    String sku,
    String name,
    boolean available,
    BigDecimal currentPrice,
    Integer availableStock,
    String unavailableReason
) {

    public VerifiedProduct {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        if (available) {
            requireOrderableAttributes(sku, name, currentPrice);
        }
    }

    private static void requireOrderableAttributes(String sku, String name, BigDecimal currentPrice) {
        Objects.requireNonNull(sku, "SKU cannot be null for an available product");
        Objects.requireNonNull(name, "Product name cannot be null for an available product");
        Objects.requireNonNull(currentPrice, "Current price cannot be null for an available product");
        if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Current price must be greater than zero");
        }
    }

}
