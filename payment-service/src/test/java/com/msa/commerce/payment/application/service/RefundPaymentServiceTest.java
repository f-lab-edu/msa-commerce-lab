package com.msa.commerce.payment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.payment.application.port.in.command.RefundPaymentCommand;
import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.InvalidPaymentStateException;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundPaymentService 단위 테스트")
class RefundPaymentServiceTest {

    private static final BigDecimal PARTIAL_AMOUNT = new BigDecimal("5000.0000");

    private static final BigDecimal REMAINING_AMOUNT = new BigDecimal("10000.0000");

    private static final BigDecimal AMOUNT = new BigDecimal("15000.0000");

    private static final String REASON = "상품 불량";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private PaymentResultRecorder paymentResultRecorder;

    private RefundPaymentService refundPaymentService;

    @BeforeEach
    void setUp() {
        refundPaymentService = new RefundPaymentService(
            paymentRepository, paymentGatewayPort, paymentResultRecorder, new PaymentResponseMapper());
    }

    @Test
    @DisplayName("금액을 생략하면 잔여 금액 전액을 환불한다")
    void refundsFullAmountWhenAmountOmitted() {
        Payment payment = capturedPayment();
        givenFound(payment);
        givenGatewayApproves(payment, AMOUNT);
        givenRecorded();

        var response = refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), null, REASON, null));

        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.refundAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(response.refundReason()).isEqualTo(REASON);
        assertThat(response.refundedAt()).isNotNull();
        then(paymentGatewayPort).should().refund(payment, AMOUNT, REASON);
    }

    @Test
    @DisplayName("부분 환불하면 PARTIAL_REFUNDED 가 되고 잔여 금액이 줄어든다")
    void refundsPartially() {
        Payment payment = capturedPayment();
        givenFound(payment);
        givenGatewayApproves(payment, PARTIAL_AMOUNT);
        givenRecorded();

        var response = refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), PARTIAL_AMOUNT, REASON, null));

        assertThat(response.status()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
        assertThat(response.refundAmount()).isEqualByComparingTo("5000.0000");
        assertThat(payment.refundableAmount()).isEqualByComparingTo("10000.0000");
    }

    @Test
    @DisplayName("부분 환불을 반복해 전액에 도달하면 REFUNDED 가 된다")
    void accumulatesPartialRefunds() {
        Payment payment = capturedPayment();
        payment.refund(REMAINING_AMOUNT, REASON);
        givenFound(payment);
        givenGatewayApproves(payment, PARTIAL_AMOUNT);
        givenRecorded();

        var response = refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), PARTIAL_AMOUNT, REASON, null));

        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.refundAmount()).isEqualByComparingTo(AMOUNT);
    }

    @Test
    @DisplayName("잔여 금액을 넘는 환불은 거부한다")
    void rejectsRefundOverRemaining() {
        Payment payment = capturedPayment();
        givenFound(payment);
        givenGatewayApproves(payment, new BigDecimal("20000.0000"));

        assertThatThrownBy(() -> refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), new BigDecimal("20000.0000"), REASON, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds the refundable amount");

        then(paymentResultRecorder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("매입되지 않은 결제는 환불할 수 없다")
    void rejectsRefundOfUnsettledPayment() {
        Payment payment = pendingPayment();
        givenFound(payment);
        givenGatewayApproves(payment, AMOUNT);

        assertThatThrownBy(() -> refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), null, REASON, null)))
            .isInstanceOf(InvalidPaymentStateException.class)
            .hasMessageContaining("cannot be refunded");
    }

    @Test
    @DisplayName("PG사가 환불을 거절하면 로컬 상태를 바꾸지 않는다")
    void keepsStateWhenGatewayRejects() {
        Payment payment = capturedPayment();
        givenFound(payment);
        given(paymentGatewayPort.refund(eq(payment), any(BigDecimal.class), anyString()))
            .willReturn(PaymentGatewayActionResult.rejected("REFUND_WINDOW_CLOSED", "환불 가능 기간이 지났습니다"));

        assertThatThrownBy(() -> refundPaymentService.refund(new RefundPaymentCommand(
            payment.getPaymentId(), null, REASON, null)))
            .isInstanceOf(PaymentGatewayRejectedException.class)
            .hasMessageContaining("환불 가능 기간");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    @DisplayName("환불은 payment.result 이벤트를 발행하지 않는다")
    void doesNotPublishPaymentResult() {
        Payment payment = capturedPayment();
        givenFound(payment);
        givenGatewayApproves(payment, AMOUNT);
        givenRecorded();

        refundPaymentService.refund(new RefundPaymentCommand(payment.getPaymentId(), null, REASON, null));

        then(paymentResultRecorder).should().recordOnly(payment);
        then(paymentResultRecorder).should(never()).recordAndPublish(any(), any());
    }

    private void givenFound(Payment payment) {
        given(paymentRepository.findByPaymentId(payment.getPaymentId())).willReturn(Optional.of(payment));
    }

    private void givenGatewayApproves(Payment payment, BigDecimal amount) {
        given(paymentGatewayPort.refund(payment, amount, REASON))
            .willReturn(PaymentGatewayActionResult.approved("RFD-1"));
    }

    private void givenRecorded() {
        given(paymentResultRecorder.recordOnly(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    private Payment capturedPayment() {
        Payment payment = pendingPayment();
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

    private Payment pendingPayment() {
        return Payment.request(UUID.randomUUID(), 1001L, AMOUNT, "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
    }

}
