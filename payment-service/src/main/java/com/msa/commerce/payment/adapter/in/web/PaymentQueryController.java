package com.msa.commerce.payment.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.payment.application.port.in.GetPaymentUseCase;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentQueryController {

    private final GetPaymentUseCase getPaymentUseCase;

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(getPaymentUseCase.getByPaymentId(paymentId));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(getPaymentUseCase.getByOrderId(orderId));
    }

}
