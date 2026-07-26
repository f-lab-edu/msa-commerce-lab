package com.msa.commerce.materializedview.domain;

public enum OrderStatusCategory {

    ACTIVE,
    COMPLETED,
    CANCELLED;

    public static OrderStatusCategory from(String status) {
        if (status == null) {
            return ACTIVE;
        }
        return switch (status) {
            case "DELIVERED" -> COMPLETED;
            case "CANCELLED", "REFUNDED", "FAILED" -> CANCELLED;
            default -> ACTIVE;
        };
    }

}
