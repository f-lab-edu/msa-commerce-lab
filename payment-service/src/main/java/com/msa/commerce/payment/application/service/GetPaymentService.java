package com.msa.commerce.payment.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.msa.commerce.payment.application.port.in.GetPaymentUseCase;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPaymentService implements GetPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final PaymentResponseMapper paymentResponseMapper;

    @Override
    public PaymentResponse getByPaymentId(UUID paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
            .map(paymentResponseMapper::toResponse)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
    }

    @Override
    public List<PaymentResponse> getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
            .stream()
            .map(paymentResponseMapper::toResponse)
            .toList();
    }

}
