package com.msa.commerce.payment.application.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.payment.application.port.in.RefundPaymentUseCase;
import com.msa.commerce.payment.application.port.in.command.RefundPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 환불은 payment.result 를 발행하지 않는다.
 * 이미 PAID 로 넘어간 주문은 order-orchestrator 에서 취소가 불가능해서
 * CANCELLED 이벤트를 보내봐야 경고 로그만 남는다. 정산/환불 도메인(#47)이
 * 별도 이벤트를 정의하기 전까지는 결제 레코드에만 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPaymentService implements RefundPaymentUseCase {

    private final PaymentRepository paymentRepository;

    private final PaymentGatewayPort paymentGatewayPort;

    private final PaymentResultRecorder paymentResultRecorder;

    private final PaymentResponseMapper paymentResponseMapper;

    @Override
    @ValidateCommand
    public PaymentResponse refund(RefundPaymentCommand command) {
        Payment payment = paymentRepository.findByPaymentId(command.paymentId())
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + command.paymentId()));

        BigDecimal requested = command.amount() != null ? command.amount() : payment.refundableAmount();

        PaymentGatewayActionResult result = paymentGatewayPort.refund(payment, requested, command.reason());
        if (!result.approved()) {
            throw new PaymentGatewayRejectedException(
                "Payment gateway rejected the refund: " + result.failureReason());
        }

        payment.refund(requested, command.reason());

        Payment refunded = paymentResultRecorder.recordOnly(payment);

        log.info("Payment refunded: paymentId={}, orderId={}, amount={}, status={}",
            refunded.getPaymentId(), refunded.getOrderId(), requested, refunded.getStatus());

        return paymentResponseMapper.toResponse(refunded);
    }

}
