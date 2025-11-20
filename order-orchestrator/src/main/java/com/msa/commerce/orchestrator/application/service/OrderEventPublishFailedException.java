package com.msa.commerce.orchestrator.application.service;

public class OrderEventPublishFailedException extends RuntimeException {

    public OrderEventPublishFailedException(String message) {
        super(message);
    }

    public OrderEventPublishFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
