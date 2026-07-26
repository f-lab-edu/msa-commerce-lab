package com.msa.commerce.payment.application.port.out;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.InternalException;

// PG사와의 통신 자체가 실패한 경우. 거절(DECLINED)과 달리 재시도할 여지가 있다.
public class PaymentGatewayException extends InternalException {

    public PaymentGatewayException(String message) {
        super(message, ErrorCode.PAYMENT_GATEWAY_ERROR.getCode());
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, ErrorCode.PAYMENT_GATEWAY_ERROR.getCode(), cause);
    }

}
