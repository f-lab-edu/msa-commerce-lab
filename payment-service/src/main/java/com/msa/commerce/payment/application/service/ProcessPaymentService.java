package com.msa.commerce.payment.application.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.payment.application.port.in.ProcessPaymentUseCase;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.port.out.PaymentGatewayException;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * PG 호출이 DB 트랜잭션 안에 들어가면 커넥션을 외부 응답 시간만큼 붙잡고,
 * 승인 성공 후 롤백되면 결제 기록만 사라지는 사고가 난다.
 * 그래서 클래스 레벨 @Transactional 없이 "PENDING 저장 → PG 호출 → 결과 저장" 을
 * 각각 별개의 트랜잭션(PaymentRepositoryImpl)으로 끊는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentService implements ProcessPaymentUseCase {

    private static final String GATEWAY_ERROR_CODE = "GATEWAY_ERROR";

    private final PaymentRepository paymentRepository;

    private final PaymentGatewayPort paymentGatewayPort;

    private final PaymentResponseMapper paymentResponseMapper;

    @Override
    @ValidateCommand
    public PaymentResponse process(ProcessPaymentCommand command) {
        Payment pending = createPendingPayment(command);

        log.info("Payment requested: paymentId={}, orderId={}, amount={} {}",
            pending.getPaymentId(), pending.getOrderId(), pending.getAmount(), pending.getCurrency());

        applyGatewayOutcome(pending);

        Payment settled = paymentRepository.save(pending);

        log.info("Payment processed: paymentId={}, orderId={}, status={}",
            settled.getPaymentId(), settled.getOrderId(), settled.getStatus());

        return paymentResponseMapper.toResponse(settled);
    }

    private Payment createPendingPayment(ProcessPaymentCommand command) {
        paymentRepository.findActiveByOrderId(command.orderId())
            .ifPresent(existing -> {
                throw new DuplicatePaymentException(command.orderId());
            });

        Payment payment = Payment.request(
            command.orderId(),
            command.customerId(),
            command.amount(),
            command.currency(),
            command.paymentMethod(),
            paymentGatewayPort.providerName(),
            command.paymentDetails());

        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            // uk_payments_active_order 위반 — 위 조회와 저장 사이에 다른 요청이 먼저 들어온 경우
            log.warn("Concurrent payment request rejected for order: {}", command.orderId());
            throw new DuplicatePaymentException(command.orderId());
        }
    }

    private void applyGatewayOutcome(Payment payment) {
        PaymentGatewayResult result;
        try {
            result = paymentGatewayPort.authorize(payment);
        } catch (PaymentGatewayException e) {
            log.error("Payment gateway call failed: paymentId={}, reason={}",
                payment.getPaymentId(), e.getMessage(), e);
            payment.fail(GATEWAY_ERROR_CODE, e.getMessage());
            return;
        }

        switch (result.outcome()) {
            case AUTHORIZED -> payment.authorize(
                result.externalPaymentId(), result.gatewayTransactionId(), result.approvalNumber());
            case CAPTURED -> payment.capture(
                result.externalPaymentId(), result.gatewayTransactionId(), result.approvalNumber());
            case DECLINED -> payment.fail(result.failureCode(), result.failureReason());
        }
    }

}
