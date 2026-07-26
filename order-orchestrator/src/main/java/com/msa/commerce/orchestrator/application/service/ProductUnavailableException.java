package com.msa.commerce.orchestrator.application.service;

import com.msa.commerce.common.exception.ValidationException;

public class ProductUnavailableException extends ValidationException {

    private static final String ERROR_CODE = "PRODUCT_UNAVAILABLE";

    public ProductUnavailableException(String message) {
        super(message, ERROR_CODE);
    }

}
