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

import com.msa.commerce.payment.application.port.in.command.CancelPaymentCommand;
import com.msa.commerce.payment.application.port.out.PaymentGatewayActionResult;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.InvalidPaymentStateException;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPaymentService 단위 테스트")
class CancelPaymentServiceTest {

    private static final String REASON = "고객 변심";

    private static final String CORRELATION_ID = "corr-1";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private PaymentResultRecorder paymentResultRecorder;

    private CancelPaymentService cancelPaymentService;

    @BeforeEach
    void setUp() {
        cancelPaymentService = new CancelPaymentService(
            paymentRepository, paymentGatewayPort, paymentResultRecorder, new PaymentResponseMapper());
    }

    @Test
    @DisplayName("승인 상태의 결제를 취소하면 CANCELLED 가 되고 이벤트가 적재된다")
    void cancelAuthorizedPayment() {
        Payment payment = authorizedPayment();
        givenFound(payment);
        given(paymentGatewayPort.cancel(payment, REASON))
            .willReturn(PaymentGatewayActionResult.approved("CNL-1"));
        given(paymentResultRecorder.recordAndPublish(any(Payment.class), any()))
            .willAnswer(invocation -> invocation.getArgument(0));

        var response = cancelPaymentService.cancel(command(payment.getPaymentId()));

        assertThat(response.status()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(response.cancelReason()).isEqualTo(REASON);
        assertThat(response.cancelledAt()).isNotNull();
        then(paymentResultRecorder).should().recordAndPublish(payment, CORRELATION_ID);
    }

    @Test
    @DisplayName("존재하지 않는 결제를 취소하면 예외가 발생한다")
    void rejectsUnknownPayment() {
        UUID missing = UUID.randomUUID();
        given(paymentRepository.findByPaymentId(missing)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cancelPaymentService.cancel(command(missing)))
            .isInstanceOf(PaymentNotFoundException.class);

        then(paymentGatewayPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 매입된 결제는 취소할 수 없다")
    void rejectsCapturedPayment() {
        Payment payment = capturedPayment();
        givenFound(payment);
        given(paymentGatewayPort.cancel(payment, REASON))
            .willReturn(PaymentGatewayActionResult.approved("CNL-1"));

        assertThatThrownBy(() -> cancelPaymentService.cancel(command(payment.getPaymentId())))
            .isInstanceOf(InvalidPaymentStateException.class)
            .hasMessageContaining("refund it instead");

        then(paymentResultRecorder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("PG사가 취소를 거절하면 로컬 상태를 바꾸지 않는다")
    void keepsStateWhenGatewayRejects() {
        Payment payment = authorizedPayment();
        givenFound(payment);
        given(paymentGatewayPort.cancel(payment, REASON))
            .willReturn(PaymentGatewayActionResult.rejected("ALREADY_SETTLED", "이미 정산된 거래"));

        assertThatThrownBy(() -> cancelPaymentService.cancel(command(payment.getPaymentId())))
            .isInstanceOf(PaymentGatewayRejectedException.class)
            .hasMessageContaining("이미 정산된 거래");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        then(paymentResultRecorder).shouldHaveNoInteractions();
    }

    private void givenFound(Payment payment) {
        given(paymentRepository.findByPaymentId(payment.getPaymentId())).willReturn(Optional.of(payment));
    }

    private CancelPaymentCommand command(UUID paymentId) {
        return new CancelPaymentCommand(paymentId, REASON, CORRELATION_ID);
    }

    private Payment authorizedPayment() {
        Payment payment = pendingPayment();
        payment.authorize("EXT-1", "TXN-1", "0001");
        return payment;
    }

    private Payment capturedPayment() {
        Payment payment = pendingPayment();
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

    private Payment pendingPayment() {
        return Payment.request(UUID.randomUUID(), 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.VIRTUAL_ACCOUNT, "MOCK_PG", null);
    }

}
