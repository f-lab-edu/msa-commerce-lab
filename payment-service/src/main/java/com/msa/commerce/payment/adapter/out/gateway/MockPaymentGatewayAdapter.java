package com.msa.commerce.payment.adapter.out.gateway;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.domain.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 실제 PG사 연동 전까지 사용하는 대체 어댑터. 금액 한도만 보고 결정론적으로 응답한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final String LIMIT_EXCEEDED_CODE = "LIMIT_EXCEEDED";

    private final PaymentGatewayProperties properties;

    @Override
    public String providerName() {
        return properties.providerName();
    }

    @Override
    public PaymentGatewayResult authorize(Payment payment) {
        if (payment.getAmount().compareTo(properties.mock().approvalLimit()) > 0) {
            log.info("Mock gateway declined payment: paymentId={}, amount={}, limit={}",
                payment.getPaymentId(), payment.getAmount(), properties.mock().approvalLimit());

            return PaymentGatewayResult.declined(LIMIT_EXCEEDED_CODE,
                "Amount exceeds the approval limit of " + properties.mock().approvalLimit());
        }

        String externalPaymentId = "MOCK-" + payment.getPaymentId();
        String transactionId = "TXN-" + UUID.randomUUID();
        String approvalNumber = generateApprovalNumber(payment);

        log.info("Mock gateway approved payment: paymentId={}, method={}, transactionId={}",
            payment.getPaymentId(), payment.getPaymentMethod(), transactionId);

        return payment.getPaymentMethod().requiresDeferredSettlement()
            ? PaymentGatewayResult.authorized(externalPaymentId, transactionId, approvalNumber)
            : PaymentGatewayResult.captured(externalPaymentId, transactionId, approvalNumber);
    }

    @Override
    public PaymentGatewayActionResult cancel(Payment payment, String reason) {
        log.info("Mock gateway cancelled payment: paymentId={}, reason={}", payment.getPaymentId(), reason);

        return PaymentGatewayActionResult.approved("CNL-" + UUID.randomUUID());
    }

    @Override
    public PaymentGatewayActionResult refund(Payment payment, BigDecimal amount, String reason) {
        log.info("Mock gateway refunded payment: paymentId={}, amount={}, reason={}",
            payment.getPaymentId(), amount, reason);

        return PaymentGatewayActionResult.approved("RFD-" + UUID.randomUUID());
    }

    private String generateApprovalNumber(Payment payment) {
        return Long.toString(Math.abs(payment.getPaymentId().getMostSignificantBits() % 100_000_000L));
    }

}
