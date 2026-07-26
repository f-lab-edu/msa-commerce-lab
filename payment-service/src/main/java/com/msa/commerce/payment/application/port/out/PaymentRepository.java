package com.msa.commerce.payment.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msa.commerce.payment.domain.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByPaymentId(UUID paymentId);

    List<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findActiveByOrderId(UUID orderId);

}
