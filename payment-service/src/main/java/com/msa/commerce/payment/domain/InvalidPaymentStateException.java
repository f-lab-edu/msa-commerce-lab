package com.msa.commerce.payment.domain;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.InternalException;

public class InvalidPaymentStateException extends InternalException {

    public InvalidPaymentStateException(String message) {
        super(message, ErrorCode.PAYMENT_INVALID_STATUS_TRANSITION.getCode());
    }

    public static InvalidPaymentStateException transition(PaymentStatus from, PaymentStatus to) {
        return new InvalidPaymentStateException(
            String.format("Cannot transition payment from %s to %s", from, to));
    }

}
