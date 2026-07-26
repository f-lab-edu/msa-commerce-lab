package com.msa.commerce.payment.adapter.in.web.mapper;

import org.springframework.stereotype.Component;

import com.msa.commerce.payment.adapter.in.web.dto.request.ProcessPaymentRequest;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;

@Component
public class PaymentCommandMapper {

    private static final String DEFAULT_CURRENCY = "KRW";

    public ProcessPaymentCommand toProcessPaymentCommand(ProcessPaymentRequest request) {
        return new ProcessPaymentCommand(
            request.orderId(),
            request.customerId(),
            request.amount(),
            request.currency() != null ? request.currency() : DEFAULT_CURRENCY,
            request.paymentMethod(),
            request.paymentDetails());
    }

}
