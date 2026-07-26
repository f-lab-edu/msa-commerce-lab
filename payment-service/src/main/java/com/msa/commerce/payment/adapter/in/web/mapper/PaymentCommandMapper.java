package com.msa.commerce.payment.adapter.in.web.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msa.commerce.payment.adapter.in.web.dto.request.CancelPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.dto.request.ProcessPaymentRequest;
import com.msa.commerce.payment.adapter.in.web.dto.request.RefundPaymentRequest;
import com.msa.commerce.payment.application.port.in.command.CancelPaymentCommand;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.in.command.RefundPaymentCommand;

@Component
public class PaymentCommandMapper {

    private static final String DEFAULT_CURRENCY = "KRW";

    public ProcessPaymentCommand toProcessPaymentCommand(ProcessPaymentRequest request, String correlationId) {
        return new ProcessPaymentCommand(
            request.orderId(),
            request.customerId(),
            request.amount(),
            request.currency() != null ? request.currency() : DEFAULT_CURRENCY,
            request.paymentMethod(),
            request.paymentDetails(),
            correlationId);
    }

    public CancelPaymentCommand toCancelPaymentCommand(UUID paymentId, CancelPaymentRequest request,
        String correlationId) {
        return new CancelPaymentCommand(paymentId, request.reason(), correlationId);
    }

    public RefundPaymentCommand toRefundPaymentCommand(UUID paymentId, RefundPaymentRequest request,
        String correlationId) {
        return new RefundPaymentCommand(paymentId, request.amount(), request.reason(), correlationId);
    }

}
