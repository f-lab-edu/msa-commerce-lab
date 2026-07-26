package com.msa.commerce.payment.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.msa.commerce.payment.adapter.in.web.dto.request.CancelPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.dto.request.ProcessPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.dto.request.RefundPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.mapper.PaymentCommandMapper;
import com.msa.commerce.payment.application.port.in.CancelPaymentUseCase;
import com.msa.commerce.payment.application.port.in.ProcessPaymentUseCase;
import com.msa.commerce.payment.application.port.in.RefundPaymentUseCase;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentCommandController {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final ProcessPaymentUseCase processPaymentUseCase;

    private final CancelPaymentUseCase cancelPaymentUseCase;

    private final RefundPaymentUseCase refundPaymentUseCase;

    private final PaymentCommandMapper paymentCommandMapper;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
        @Valid @RequestBody ProcessPaymentRequest request,
        @RequestHeader(value = CORRELATION_ID_HEADER, required = false) String correlationId) {

        PaymentResponse response = processPaymentUseCase.process(
            paymentCommandMapper.toProcessPaymentCommand(request, correlationId));

        return ResponseEntity
            .created(UriComponentsBuilder.fromPath("/api/v1/payments/{paymentId}")
                .buildAndExpand(response.paymentId())
                .toUri())
            .body(response);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
        @PathVariable UUID paymentId,
        @Valid @RequestBody CancelPaymentRequest request,
        @RequestHeader(value = CORRELATION_ID_HEADER, required = false) String correlationId) {

        return ResponseEntity.ok(cancelPaymentUseCase.cancel(
            paymentCommandMapper.toCancelPaymentCommand(paymentId, request, correlationId)));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
        @PathVariable UUID paymentId,
        @Valid @RequestBody RefundPaymentRequest request,
        @RequestHeader(value = CORRELATION_ID_HEADER, required = false) String correlationId) {

        return ResponseEntity.ok(refundPaymentUseCase.refund(
            paymentCommandMapper.toRefundPaymentCommand(paymentId, request, correlationId)));
    }

}
