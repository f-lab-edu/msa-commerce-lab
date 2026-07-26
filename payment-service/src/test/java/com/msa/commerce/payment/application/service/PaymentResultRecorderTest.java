package com.msa.commerce.payment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.payment.application.port.out.PaymentEventPublisher;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentResultRecorder 단위 테스트")
class PaymentResultRecorderTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentResultRecorder paymentResultRecorder;

    @Test
    @DisplayName("저장한 결제를 기준으로 이벤트를 적재한다")
    void publishesUsingSavedPayment() {
        Payment payment = capturedPayment();
        Payment saved = capturedPayment();
        given(paymentRepository.save(payment)).willReturn(saved);

        assertThat(paymentResultRecorder.recordAndPublish(payment, "corr-1")).isSameAs(saved);

        // 저장 후의 인스턴스(채번된 id/version 포함)로 이벤트를 만들어야 한다
        InOrder inOrder = inOrder(paymentRepository, paymentEventPublisher);
        inOrder.verify(paymentRepository).save(payment);
        inOrder.verify(paymentEventPublisher).publishPaymentResult(saved, "corr-1");
    }

    @Test
    @DisplayName("record 는 이벤트를 적재하지 않는다")
    void recordDoesNotPublish() {
        Payment payment = capturedPayment();
        given(paymentRepository.save(payment)).willReturn(payment);

        assertThat(paymentResultRecorder.record(payment)).isSameAs(payment);

        then(paymentEventPublisher).should(never()).publishPaymentResult(any(), any());
    }

    private Payment capturedPayment() {
        Payment payment = Payment.request(UUID.randomUUID(), 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

}
