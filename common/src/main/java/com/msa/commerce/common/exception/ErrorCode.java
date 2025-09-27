package com.msa.commerce.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_INPUT("E1001", "Invalid input provided"),
    VALIDATION_FAILED("E1002", "Validation failed"),

    PRODUCT_NAME_REQUIRED("P1001", "Product name is required"),
    PRODUCT_NAME_TOO_LONG("P1002", "Product name is too long"),
    PRODUCT_PRICE_INVALID("P1003", "Product price is invalid"),
    PRODUCT_STOCK_INVALID("P1004", "Product stock quantity is invalid"),
    PRODUCT_CATEGORY_REQUIRED("P1005", "Product category is required"),
    PRODUCT_NOT_FOUND("P1006", "Product not found"),
    PRODUCT_NAME_DUPLICATE("P1007", "Product name already exists"),
    PRODUCT_SKU_DUPLICATE("P1008", "Product SKU already exists"),
    INSUFFICIENT_STOCK("P1009", "Insufficient stock available"),
    PRODUCT_UPDATE_NOT_ALLOWED("P1010", "Product cannot be updated"),
    PRODUCT_NO_CHANGES_PROVIDED("P1011", "No changes provided for update"),

    USER_NOT_FOUND("U1001", "User not found"),
    USER_EMAIL_DUPLICATE("U1002", "Email already exists"),

    ORDER_NOT_FOUND("O1001", "Order not found"),
    ORDER_ALREADY_CANCELLED("O1002", "Order already cancelled"),

    INTERNAL_SERVER_ERROR("S1001", "Internal server error"),
    EXTERNAL_SERVICE_ERROR("S1002", "External service error");

    private final String code;

    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

}
