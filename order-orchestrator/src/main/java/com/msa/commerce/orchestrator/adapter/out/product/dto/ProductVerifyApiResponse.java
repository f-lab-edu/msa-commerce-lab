package com.msa.commerce.orchestrator.adapter.out.product.dto;

import java.math.BigDecimal;
import java.util.List;

import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.VerifiedProduct;

public record ProductVerifyApiResponse(Boolean allAvailable, List<Result> results) {

    public ProductVerification toDomain() {
        List<VerifiedProduct> verifiedProducts = results != null
            ? results.stream().map(Result::toDomain).toList()
            : List.of();

        return new ProductVerification(Boolean.TRUE.equals(allAvailable), verifiedProducts);
    }

    public record Result(
        Long productId,
        String sku,
        String name,
        Boolean available,
        Integer availableStock,
        BigDecimal currentPrice,
        String unavailableReason
    ) {

        VerifiedProduct toDomain() {
            return new VerifiedProduct(
                productId,
                sku,
                name,
                Boolean.TRUE.equals(available),
                currentPrice,
                availableStock,
                unavailableReason
            );
        }

    }

}
