package com.msa.commerce.orchestrator.application.port.in;

import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;

public interface ProcessPaymentResultUseCase {

    void processPaymentResult(PaymentResultEvent event);

}
