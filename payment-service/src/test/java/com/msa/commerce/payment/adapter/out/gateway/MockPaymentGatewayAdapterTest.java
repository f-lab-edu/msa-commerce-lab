package com.msa.commerce.payment.adapter.out.gateway;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;

@DisplayName("MockPaymentGatewayAdapter 테스트")
class MockPaymentGatewayAdapterTest {

    private static final BigDecimal APPROVAL_LIMIT = new BigDecimal("100000.0000");

    private MockPaymentGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockPaymentGatewayAdapter(
            new PaymentGatewayProperties("MOCK_PG", new PaymentGatewayProperties.Mock(APPROVAL_LIMIT)));
    }

    @Test
    @DisplayName("PG사 이름은 설정값을 그대로 노출한다")
    void providerName() {
        assertThat(adapter.providerName()).isEqualTo("MOCK_PG");
    }

    @Test
    @DisplayName("카드 결제는 승인과 매입이 한 번에 끝난다")
    void immediateSettlementMethodIsCaptured() {
        PaymentGatewayResult result = adapter.authorize(payment(new BigDecimal("15000.0000"),
            PaymentMethod.CREDIT_CARD));

        assertThat(result.outcome()).isEqualTo(PaymentGatewayResult.Outcome.CAPTURED);
        assertThat(result.isApproved()).isTrue();
        assertThat(result.externalPaymentId()).startsWith("MOCK-");
        assertThat(result.gatewayTransactionId()).startsWith("TXN-");
        assertThat(result.approvalNumber()).isNotBlank();
        assertThat(result.failureCode()).isNull();
    }

    @Test
    @DisplayName("가상계좌는 승인만 되고 매입은 나중이다")
    void deferredSettlementMethodIsAuthorized() {
        PaymentGatewayResult result = adapter.authorize(payment(new BigDecimal("15000.0000"),
            PaymentMethod.VIRTUAL_ACCOUNT));

        assertThat(result.outcome()).isEqualTo(PaymentGatewayResult.Outcome.AUTHORIZED);
        assertThat(result.isApproved()).isTrue();
    }

    @Test
    @DisplayName("한도를 초과하면 거절된다")
    void declinesOverLimit() {
        PaymentGatewayResult result = adapter.authorize(payment(new BigDecimal("100000.0001"),
            PaymentMethod.CREDIT_CARD));

        assertThat(result.outcome()).isEqualTo(PaymentGatewayResult.Outcome.DECLINED);
        assertThat(result.isApproved()).isFalse();
        assertThat(result.failureCode()).isEqualTo("LIMIT_EXCEEDED");
        assertThat(result.failureReason()).contains(APPROVAL_LIMIT.toPlainString());
        assertThat(result.gatewayTransactionId()).isNull();
    }

    @Test
    @DisplayName("한도와 정확히 같은 금액은 승인된다")
    void approvesExactlyAtLimit() {
        PaymentGatewayResult result = adapter.authorize(payment(APPROVAL_LIMIT, PaymentMethod.CREDIT_CARD));

        assertThat(result.isApproved()).isTrue();
    }

    @Test
    @DisplayName("설정을 생략하면 기본 PG사 이름과 기본 한도가 적용된다")
    void appliesDefaultsWhenPropertiesAreMissing() {
        MockPaymentGatewayAdapter defaultAdapter =
            new MockPaymentGatewayAdapter(new PaymentGatewayProperties(null, null));

        assertThat(defaultAdapter.providerName()).isEqualTo("MOCK_PG");
        assertThat(defaultAdapter.authorize(payment(new BigDecimal("9999999.0000"), PaymentMethod.CREDIT_CARD))
            .isApproved()).isTrue();
    }

    private Payment payment(BigDecimal amount, PaymentMethod method) {
        return Payment.request(UUID.randomUUID(), 1001L, amount, "KRW", method, "MOCK_PG", null);
    }

}
