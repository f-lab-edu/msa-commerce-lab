package com.msa.commerce.payment.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.msa.commerce.payment.adapter.in.web.dto.request.ProcessPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.mapper.PaymentCommandMapper;
import com.msa.commerce.payment.application.port.in.ProcessPaymentUseCase;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentCommandController {

    private final ProcessPaymentUseCase processPaymentUseCase;

    private final PaymentCommandMapper paymentCommandMapper;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = processPaymentUseCase.process(
            paymentCommandMapper.toProcessPaymentCommand(request));

        return ResponseEntity
            .created(UriComponentsBuilder.fromPath("/api/v1/payments/{paymentId}")
                .buildAndExpand(response.paymentId())
                .toUri())
            .body(response);
    }

}
