package com.msa.commerce.orchestrator.adapter.out.product;

import com.msa.commerce.common.exception.ExternalServiceException;

public class ProductServiceException extends ExternalServiceException {

    private static final String ERROR_CODE = "PRODUCT_SERVICE_ERROR";

    public ProductServiceException(String message) {
        super(message, ERROR_CODE);
    }

    public ProductServiceException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

}
