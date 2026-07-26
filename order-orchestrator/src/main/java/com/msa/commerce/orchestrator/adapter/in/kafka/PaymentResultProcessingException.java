package com.msa.commerce.orchestrator.adapter.in.kafka;

public class PaymentResultProcessingException extends RuntimeException {

    public PaymentResultProcessingException(String message) {
        super(message);
    }

    public PaymentResultProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
