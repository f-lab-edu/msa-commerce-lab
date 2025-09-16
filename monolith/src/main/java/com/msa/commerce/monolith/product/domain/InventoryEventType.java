package com.msa.commerce.monolith.product.domain;

public enum InventoryEventType {
    STOCK_IN("입고"),
    STOCK_OUT("출고"),
    STOCK_ADJUSTMENT("재고조정"),
    STOCK_RESERVATION("재고예약"),
    STOCK_RESERVATION_RELEASE("예약해제"),
    STOCK_RESERVATION_CONFIRM("예약확정"),
    STOCK_TRANSFER("재고이동"),
    STOCK_DAMAGE("재고손상"),
    STOCK_LOST("재고분실"),
    STOCK_COUNT("재고실사");

    private final String description;

    InventoryEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}