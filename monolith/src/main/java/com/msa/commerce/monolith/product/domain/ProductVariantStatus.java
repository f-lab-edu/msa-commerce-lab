package com.msa.commerce.monolith.product.domain;

import lombok.Getter;

@Getter
public enum ProductVariantStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    OUT_OF_STOCK("재고없음");

    private final String description;

    ProductVariantStatus(String description) {
        this.description = description;
    }

}
