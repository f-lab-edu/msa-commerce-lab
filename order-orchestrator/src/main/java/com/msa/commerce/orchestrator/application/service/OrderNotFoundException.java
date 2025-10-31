package com.msa.commerce.orchestrator.application.service;

import com.msa.commerce.common.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    private static final String ERROR_CODE = "ORDER_NOT_FOUND";

    public OrderNotFoundException(String message) {
        super(message, ERROR_CODE);
    }

}
