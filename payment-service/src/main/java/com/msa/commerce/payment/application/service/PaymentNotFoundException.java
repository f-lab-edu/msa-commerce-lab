package com.msa.commerce.payment.application.service;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(String message) {
        super(message, ErrorCode.PAYMENT_NOT_FOUND.getCode());
    }

}
