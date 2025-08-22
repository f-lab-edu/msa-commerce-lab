package com.msa.commerce.common.exception;

public class ProductUpdateNotAllowedException extends BusinessException {

    public ProductUpdateNotAllowedException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ProductUpdateNotAllowedException(String message) {
        super(message, ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode());
    }

    public static ProductUpdateNotAllowedException productNotUpdatable(Long productId, String currentStatus) {
        String message = String.format("Product with ID %d cannot be updated. Current status: %s", productId,
            currentStatus);
        return new ProductUpdateNotAllowedException(message);
    }

}
