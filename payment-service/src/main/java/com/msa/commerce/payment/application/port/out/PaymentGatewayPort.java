package com.msa.commerce.payment.application.port.out;

import java.math.BigDecimal;

import com.msa.commerce.payment.domain.Payment;

public interface PaymentGatewayPort {

    String providerName();

    // 승인 요청. PG사가 거절하면 예외가 아니라 declined 결과로 돌아온다.
    PaymentGatewayResult authorize(Payment payment);

    // 매입 전 결제 취소 (승인 취소)
    PaymentGatewayActionResult cancel(Payment payment, String reason);

    // 매입 후 환불. 부분 환불을 지원한다.
    PaymentGatewayActionResult refund(Payment payment, BigDecimal amount, String reason);

}
