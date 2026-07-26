package com.msa.commerce.payment.application.port.out;

import com.msa.commerce.payment.domain.Payment;

public interface PaymentGatewayPort {

    String providerName();

    // 승인 요청. PG사가 거절하면 예외가 아니라 declined 결과로 돌아온다.
    PaymentGatewayResult authorize(Payment payment);

}
