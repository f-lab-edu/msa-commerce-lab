package com.msa.commerce.payment.application.service.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@DisplayName("PaymentResponseMapper 테스트")
class PaymentResponseMapperTest {

    private final PaymentResponseMapper mapper = new PaymentResponseMapper();

    @Test
    @DisplayName("도메인의 모든 노출 필드를 응답으로 옮긴다")
    void mapsAllExposedFields() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
            .paymentId(UUID.randomUUID())
            .orderId(UUID.randomUUID())
            .customerId(1001L)
            .amount(new BigDecimal("15000.0000"))
            .currency("KRW")
            .status(PaymentStatus.CAPTURED)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .paymentProvider("MOCK_PG")
            .gatewayTransactionId("TXN-1")
            .approvalNumber("0001")
            .failureCode("NONE")
            .failureReason("none")
            .authorizedAt(now)
            .capturedAt(now)
            .cancelledAt(now)
            .failedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .build();

        PaymentResponse response = mapper.toResponse(payment);

        assertThat(response.paymentId()).isEqualTo(payment.getPaymentId());
        assertThat(response.orderId()).isEqualTo(payment.getOrderId());
        assertThat(response.customerId()).isEqualTo(1001L);
        assertThat(response.amount()).isEqualByComparingTo("15000.0000");
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.paymentProvider()).isEqualTo("MOCK_PG");
        assertThat(response.gatewayTransactionId()).isEqualTo("TXN-1");
        assertThat(response.approvalNumber()).isEqualTo("0001");
        assertThat(response.failureCode()).isEqualTo("NONE");
        assertThat(response.failureReason()).isEqualTo("none");
        assertThat(response.authorizedAt()).isEqualTo(now);
        assertThat(response.capturedAt()).isEqualTo(now);
        assertThat(response.cancelledAt()).isEqualTo(now);
        assertThat(response.failedAt()).isEqualTo(now);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("null 이 들어오면 null 을 반환한다")
    void returnsNullForNullInput() {
        assertThat(mapper.toResponse(null)).isNull();
    }

}
