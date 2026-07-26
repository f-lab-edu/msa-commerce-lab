package com.msa.commerce.monolith.product.domain;

import lombok.Getter;

// inventory_snapshots.stock_status generated column과 동일한 3분류를 유지한다.
@Getter
public enum StockStatus {

    IN_STOCK("재고있음"),
    LOW_STOCK("재고부족"),
    OUT_OF_STOCK("재고없음");

    private final String description;

    StockStatus(String description) {
        this.description = description;
    }

}
