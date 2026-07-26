package com.msa.commerce.payment.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

public interface GetPaymentUseCase {

    PaymentResponse getByPaymentId(UUID paymentId);

    List<PaymentResponse> getByOrderId(UUID orderId);

}
