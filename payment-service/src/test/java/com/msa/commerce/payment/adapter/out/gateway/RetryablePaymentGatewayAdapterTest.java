package com.msa.commerce.payment.adapter.out.gateway;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayException;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryablePaymentGatewayAdapter 테스트")
class RetryablePaymentGatewayAdapterTest {

    private static final String TIMEOUT = "timeout";

    private static final Payment PAYMENT = Payment.request(UUID.randomUUID(), 1001L,
        new BigDecimal("15000.0000"), "KRW", PaymentMethod.CREDIT_CARD, "MOCK_PG", null);

    @Mock
    private PaymentGatewayPort delegate;

    // 테스트에서는 대기 없이 즉시 재시도한다
    private RetryablePaymentGatewayAdapter adapterWith(int maxAttempts) {
        return new RetryablePaymentGatewayAdapter(delegate,
            new PaymentGatewayProperties.Retry(maxAttempts, 0L, 1.0));
    }

    @Test
    @DisplayName("한 번에 성공하면 재시도하지 않는다")
    void succeedsWithoutRetry() {
        PaymentGatewayResult expected = PaymentGatewayResult.captured("EXT-1", "TXN-1", "0001");
        given(delegate.authorize(PAYMENT)).willReturn(expected);

        assertThat(adapterWith(3).authorize(PAYMENT)).isEqualTo(expected);

        then(delegate).should(times(1)).authorize(PAYMENT);
    }

    @Test
    @DisplayName("통신 실패 후 재시도해서 성공하면 결과를 반환한다")
    void retriesUntilSuccess() {
        PaymentGatewayResult expected = PaymentGatewayResult.captured("EXT-1", "TXN-1", "0001");
        given(delegate.authorize(PAYMENT))
            .willThrow(new PaymentGatewayException(TIMEOUT))
            .willThrow(new PaymentGatewayException(TIMEOUT))
            .willReturn(expected);

        assertThat(adapterWith(3).authorize(PAYMENT)).isEqualTo(expected);

        then(delegate).should(times(3)).authorize(PAYMENT);
    }

    @Test
    @DisplayName("최대 시도 횟수를 소진하면 예외를 던진다")
    void throwsAfterExhaustingAttempts() {
        given(delegate.authorize(PAYMENT)).willThrow(new PaymentGatewayException(TIMEOUT));

        assertThatThrownBy(() -> adapterWith(3).authorize(PAYMENT))
            .isInstanceOf(PaymentGatewayException.class)
            .hasMessageContaining("after 3 attempts")
            .hasRootCauseMessage(TIMEOUT);

        then(delegate).should(times(3)).authorize(PAYMENT);
    }

    @Test
    @DisplayName("거절(DECLINED)은 재시도하지 않는다")
    void doesNotRetryDeclined() {
        PaymentGatewayResult declined = PaymentGatewayResult.declined("LIMIT_EXCEEDED", "한도 초과");
        given(delegate.authorize(PAYMENT)).willReturn(declined);

        assertThat(adapterWith(3).authorize(PAYMENT)).isEqualTo(declined);

        then(delegate).should(times(1)).authorize(PAYMENT);
    }

    @Test
    @DisplayName("취소도 통신 실패 시 재시도한다")
    void retriesCancel() {
        PaymentGatewayActionResult expected = PaymentGatewayActionResult.approved("CNL-1");
        given(delegate.cancel(eq(PAYMENT), anyString()))
            .willThrow(new PaymentGatewayException(TIMEOUT))
            .willReturn(expected);

        assertThat(adapterWith(2).cancel(PAYMENT, "고객 변심")).isEqualTo(expected);

        then(delegate).should(times(2)).cancel(PAYMENT, "고객 변심");
    }

    @Test
    @DisplayName("환불도 통신 실패 시 재시도한다")
    void retriesRefund() {
        BigDecimal amount = new BigDecimal("5000.0000");
        PaymentGatewayActionResult expected = PaymentGatewayActionResult.approved("RFD-1");
        given(delegate.refund(eq(PAYMENT), eq(amount), anyString()))
            .willThrow(new PaymentGatewayException(TIMEOUT))
            .willReturn(expected);

        assertThat(adapterWith(2).refund(PAYMENT, amount, "상품 불량")).isEqualTo(expected);

        then(delegate).should(times(2)).refund(PAYMENT, amount, "상품 불량");
    }

    @Test
    @DisplayName("PG사 이름은 위임 대상의 값을 그대로 노출한다")
    void delegatesProviderName() {
        given(delegate.providerName()).willReturn("TOSS");

        assertThat(adapterWith(3).providerName()).isEqualTo("TOSS");
    }

    @Test
    @DisplayName("잘못된 설정값은 기본값으로 보정된다")
    void appliesRetryDefaults() {
        PaymentGatewayProperties.Retry retry = new PaymentGatewayProperties.Retry(null, null, null);

        assertThat(retry.maxAttempts()).isEqualTo(3);
        assertThat(retry.initialDelayMillis()).isEqualTo(200L);
        assertThat(retry.multiplier()).isEqualTo(2.0);
    }

}
