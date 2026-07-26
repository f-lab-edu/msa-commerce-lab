package com.msa.commerce.orchestrator.adapter.out.product.dto;

import java.util.List;

import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;

public record ProductVerifyApiRequest(List<Item> items) {

    public static ProductVerifyApiRequest from(List<ProductVerificationItem> items) {
        return new ProductVerifyApiRequest(items.stream()
            .map(item -> new Item(item.productId(), item.quantity()))
            .toList());
    }

    public record Item(Long productId, Integer quantity) {
    }

}
