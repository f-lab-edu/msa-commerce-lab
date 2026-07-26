package com.msa.commerce.payment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.out.PaymentGatewayException;
import com.msa.commerce.payment.application.port.out.PaymentGatewayPort;
import com.msa.commerce.payment.application.port.out.PaymentGatewayResult;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentService 단위 테스트")
class ProcessPaymentServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    private static final BigDecimal AMOUNT = new BigDecimal("15000.0000");

    private static final String CORRELATION_ID = "corr-1";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @Mock
    private PaymentResultRecorder paymentResultRecorder;

    private ProcessPaymentService processPaymentService;

    @BeforeEach
    void setUp() {
        processPaymentService = new ProcessPaymentService(
            paymentRepository, paymentGatewayPort, paymentResultRecorder, new PaymentResponseMapper());
    }

    @Test
    @DisplayName("PG사가 즉시 매입하면 CAPTURED 로 저장된다")
    void capturedWhenGatewayCaptures() {
        givenNoActivePayment();
        givenGatewayReturns(PaymentGatewayResult.captured("EXT-1", "TXN-1", "0001"));

        var response = processPaymentService.process(command(PaymentMethod.CREDIT_CARD));

        assertThat(response.status()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.gatewayTransactionId()).isEqualTo("TXN-1");
        assertThat(response.approvalNumber()).isEqualTo("0001");
        assertThat(response.capturedAt()).isNotNull();
    }

    @Test
    @DisplayName("PG사가 승인만 하면 AUTHORIZED 로 저장된다")
    void authorizedWhenGatewayAuthorizes() {
        givenNoActivePayment();
        givenGatewayReturns(PaymentGatewayResult.authorized("EXT-1", "TXN-1", "0001"));

        var response = processPaymentService.process(command(PaymentMethod.VIRTUAL_ACCOUNT));

        assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(response.authorizedAt()).isNotNull();
        assertThat(response.capturedAt()).isNull();
    }

    @Test
    @DisplayName("PG사가 거절하면 FAILED 로 저장되고 실패 사유가 남는다")
    void failedWhenGatewayDeclines() {
        givenNoActivePayment();
        givenGatewayReturns(PaymentGatewayResult.declined("LIMIT_EXCEEDED", "한도 초과"));

        var response = processPaymentService.process(command(PaymentMethod.CREDIT_CARD));

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureCode()).isEqualTo("LIMIT_EXCEEDED");
        assertThat(response.failureReason()).isEqualTo("한도 초과");
        assertThat(response.failedAt()).isNotNull();
    }

    @Test
    @DisplayName("PG사 통신이 끝내 실패해도 예외를 던지지 않고 FAILED 로 기록한다")
    void failedWhenGatewayThrows() {
        givenNoActivePayment();
        given(paymentGatewayPort.providerName()).willReturn("MOCK_PG");
        given(paymentGatewayPort.authorize(any(Payment.class)))
            .willThrow(new PaymentGatewayException("connection reset"));

        var response = processPaymentService.process(command(PaymentMethod.CREDIT_CARD));

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureCode()).isEqualTo("GATEWAY_ERROR");
        assertThat(response.failureReason()).isEqualTo("connection reset");
    }

    @Test
    @DisplayName("결과 확정은 correlationId 와 함께 이벤트까지 같이 기록한다")
    void publishesResultWithCorrelationId() {
        givenNoActivePayment();
        givenGatewayReturns(PaymentGatewayResult.captured("EXT-1", "TXN-1", "0001"));

        processPaymentService.process(command(PaymentMethod.CREDIT_CARD));

        then(paymentResultRecorder).should().recordAndPublish(any(Payment.class), eq(CORRELATION_ID));
    }

    @Test
    @DisplayName("PENDING 상태로 먼저 저장한 뒤 PG 를 호출한다")
    void persistsPendingBeforeCallingGateway() {
        // Payment 는 가변 객체라 ArgumentCaptor 로는 호출 시점 상태를 볼 수 없다
        List<PaymentStatus> statusesAtSave = new ArrayList<>();
        given(paymentRepository.findActiveByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            statusesAtSave.add(payment.getStatus());
            return payment;
        });
        given(paymentResultRecorder.recordAndPublish(any(Payment.class), any())).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            statusesAtSave.add(payment.getStatus());
            return payment;
        });
        givenGatewayReturns(PaymentGatewayResult.captured("EXT-1", "TXN-1", "0001"));

        processPaymentService.process(command(PaymentMethod.CREDIT_CARD));

        assertThat(statusesAtSave).containsExactly(PaymentStatus.PENDING, PaymentStatus.CAPTURED);

        InOrder inOrder = inOrder(paymentRepository, paymentGatewayPort, paymentResultRecorder);
        inOrder.verify(paymentRepository).save(any(Payment.class));
        inOrder.verify(paymentGatewayPort).authorize(any(Payment.class));
        inOrder.verify(paymentResultRecorder).recordAndPublish(any(Payment.class), any());
    }

    @Test
    @DisplayName("해당 주문에 진행 중인 결제가 있으면 거부한다")
    void rejectsDuplicatePayment() {
        Payment active = Payment.request(ORDER_ID, 1001L, AMOUNT, "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
        given(paymentRepository.findActiveByOrderId(ORDER_ID)).willReturn(Optional.of(active));

        assertThatThrownBy(() -> processPaymentService.process(command(PaymentMethod.CREDIT_CARD)))
            .isInstanceOf(DuplicatePaymentException.class)
            .hasMessageContaining(ORDER_ID.toString());

        verify(paymentRepository, never()).save(any());
        verify(paymentGatewayPort, never()).authorize(any());
        verify(paymentResultRecorder, never()).recordAndPublish(any(), any());
    }

    @Test
    @DisplayName("동시 요청으로 유니크 제약이 깨지면 중복 결제로 변환한다")
    void translatesConstraintViolationToDuplicate() {
        given(paymentRepository.findActiveByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(paymentGatewayPort.providerName()).willReturn("MOCK_PG");
        given(paymentRepository.save(any(Payment.class)))
            .willThrow(new DataIntegrityViolationException("uk_payments_active_order"));

        assertThatThrownBy(() -> processPaymentService.process(command(PaymentMethod.CREDIT_CARD)))
            .isInstanceOf(DuplicatePaymentException.class);

        verify(paymentGatewayPort, never()).authorize(any());
    }

    private void givenNoActivePayment() {
        given(paymentRepository.findActiveByOrderId(ORDER_ID)).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(paymentResultRecorder.recordAndPublish(any(Payment.class), any()))
            .willAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenGatewayReturns(PaymentGatewayResult result) {
        given(paymentGatewayPort.providerName()).willReturn("MOCK_PG");
        given(paymentGatewayPort.authorize(any(Payment.class))).willReturn(result);
    }

    private ProcessPaymentCommand command(PaymentMethod method) {
        return new ProcessPaymentCommand(ORDER_ID, 1001L, AMOUNT, "KRW", method, null, CORRELATION_ID);
    }

}
