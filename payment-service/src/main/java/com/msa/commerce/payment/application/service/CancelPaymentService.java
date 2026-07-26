package com.msa.commerce.payment.application.service;

import org.springframework.stereotype.Service;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.payment.application.port.in.CancelPaymentUseCase;
import com.msa.commerce.payment.application.port.in.command.CancelPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelPaymentService implements CancelPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final PaymentGatewayPort paymentGatewayPort;

    private final PaymentResultRecorder paymentResultRecorder;

    private final PaymentResponseMapper paymentResponseMapper;

    @Override
    @ValidateCommand
    public PaymentResponse cancel(CancelPaymentCommand command) {
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + command.paymentId()));

        // PG 취소가 실패하면 로컬 상태를 바꾸지 않는다. 여기서 던지는 예외가 곧 응답이다.
        PaymentGatewayActionResult result = paymentGatewayPort.cancel(payment, command.reason());
        if (!result.approved()) {
            throw new PaymentGatewayRejectedException(
                "Payment gateway rejected the cancellation: " + result.failureReason());
        }

        payment.cancel(command.reason());

        Payment cancelled = paymentResultRecorder.recordAndPublish(payment, command.correlationId());

        log.info("Payment cancelled: paymentId={}, orderId={}, reason={}",
            cancelled.getPaymentId(), cancelled.getOrderId(), command.reason());

        return paymentResponseMapper.toResponse(cancelled);
    }

}
