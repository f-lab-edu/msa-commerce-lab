package com.msa.commerce.monolith.product.domain.event;

import com.msa.commerce.monolith.product.domain.Product;

public record ProductEvent(
    Product product,
    Long productId,
    EventType eventType
) {

    public enum EventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED
    }

    public static ProductEvent productCreated(Product product) {
        return new ProductEvent(product, product.getId(), EventType.PRODUCT_CREATED);
    }

    public static ProductEvent productUpdated(Product product) {
        return new ProductEvent(product, product.getId(), EventType.PRODUCT_UPDATED);
    }

    public static ProductEvent productDeleted(Product product) {
        return new ProductEvent(product, product.getId(), EventType.PRODUCT_DELETED);
    }
}