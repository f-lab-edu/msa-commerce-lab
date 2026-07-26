package com.msa.commerce.orchestrator.domain.vo;

import java.util.Objects;

public record ProductVerificationItem(Long productId, Integer quantity) {

    public ProductVerificationItem {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

}
