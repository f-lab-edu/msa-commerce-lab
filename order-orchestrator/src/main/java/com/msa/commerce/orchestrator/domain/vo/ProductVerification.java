package com.msa.commerce.orchestrator.domain.vo;

import java.util.List;

public record ProductVerification(boolean allAvailable, List<VerifiedProduct> results) {

    public ProductVerification {
        results = results != null ? List.copyOf(results) : List.of();
    }

    public VerifiedProduct find(Long productId) {
        return results.stream()
            .filter(result -> result.productId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Verification result is missing for productId: " + productId));
    }

    public List<VerifiedProduct> unavailableProducts() {
        return results.stream()
            .filter(result -> !result.available())
            .toList();
    }

}
