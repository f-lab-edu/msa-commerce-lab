package com.msa.commerce.payment.application.service;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.InternalException;

// PG사가 취소/환불 요청을 거절한 경우. 통신 실패와 달리 재시도해도 결과가 같다.
public class PaymentGatewayRejectedException extends InternalException {

    public PaymentGatewayRejectedException(String message) {
        super(message, ErrorCode.PAYMENT_GATEWAY_ERROR.getCode());
    }

}
