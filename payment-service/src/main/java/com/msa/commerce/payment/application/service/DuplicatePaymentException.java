package com.msa.commerce.payment.application.service;

import java.util.UUID;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;

public class DuplicatePaymentException extends DuplicateResourceException {

    public DuplicatePaymentException(UUID orderId) {
        super("An active payment already exists for order: " + orderId,
            ErrorCode.PAYMENT_ALREADY_IN_PROGRESS.getCode());
    }

}
