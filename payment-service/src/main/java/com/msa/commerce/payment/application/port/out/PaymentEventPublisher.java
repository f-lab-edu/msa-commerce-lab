package com.msa.commerce.payment.application.port.out;

import com.msa.commerce.payment.domain.Payment;

public interface PaymentEventPublisher {

    // 반드시 결제 상태 변경과 같은 트랜잭션 안에서 호출해야 한다.
    void publishPaymentResult(Payment payment, String correlationId);

}
