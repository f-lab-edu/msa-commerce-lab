package com.msa.commerce.monolith.product.domain;

public enum StockStatus {
    IN_STOCK("재고있음"),
    LOW_STOCK("재고부족"),
    OUT_OF_STOCK("재고없음");

    private final String description;

    StockStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
