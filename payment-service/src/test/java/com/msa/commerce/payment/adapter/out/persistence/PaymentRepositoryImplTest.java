package com.msa.commerce.payment.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRepositoryImpl 단위 테스트")
class PaymentRepositoryImplTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    private PaymentJpaRepository paymentJpaRepository;

    private PaymentRepositoryImpl paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepositoryImpl(paymentJpaRepository, new PaymentDomainMapper());
    }

    @Test
    @DisplayName("새 결제는 엔티티를 새로 만들어 저장한다")
    void insertsNewPayment() {
        Payment payment = pendingPayment();
        given(paymentJpaRepository.findByPaymentId(payment.getPaymentId())).willReturn(Optional.empty());
        given(paymentJpaRepository.save(any(PaymentJpaEntity.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentRepository.save(payment);

        assertThat(saved.getPaymentId()).isEqualTo(payment.getPaymentId());
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("기존 결제는 관리 중인 엔티티를 갱신해 낙관적 락 버전을 유지한다")
    void updatesExistingPayment() {
        Payment payment = pendingPayment();
        PaymentJpaEntity existing = PaymentJpaEntity.from(payment);
        given(paymentJpaRepository.findByPaymentId(payment.getPaymentId())).willReturn(Optional.of(existing));
        given(paymentJpaRepository.save(any(PaymentJpaEntity.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        payment.capture("EXT-1", "TXN-1", "0001");
        Payment saved = paymentRepository.save(payment);

        ArgumentCaptor<PaymentJpaEntity> captor = ArgumentCaptor.forClass(PaymentJpaEntity.class);
        verify(paymentJpaRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    @DisplayName("결제 ID 로 조회하면 도메인으로 변환해 돌려준다")
    void findsByPaymentId() {
        Payment payment = pendingPayment();
        given(paymentJpaRepository.findByPaymentId(payment.getPaymentId()))
            .willReturn(Optional.of(PaymentJpaEntity.from(payment)));

        assertThat(paymentRepository.findByPaymentId(payment.getPaymentId()))
            .get()
            .extracting(Payment::getOrderId)
            .isEqualTo(ORDER_ID);
    }

    @Test
    @DisplayName("없는 결제 ID 는 빈 Optional 을 돌려준다")
    void findsNothingForUnknownPaymentId() {
        UUID missing = UUID.randomUUID();
        given(paymentJpaRepository.findByPaymentId(missing)).willReturn(Optional.empty());

        assertThat(paymentRepository.findByPaymentId(missing)).isEmpty();
    }

    @Test
    @DisplayName("주문 ID 로 조회하면 최신순 결제 이력을 돌려준다")
    void findsByOrderId() {
        given(paymentJpaRepository.findByOrderIdOrderByCreatedAtDesc(ORDER_ID))
            .willReturn(List.of(PaymentJpaEntity.from(pendingPayment()), PaymentJpaEntity.from(pendingPayment())));

        assertThat(paymentRepository.findByOrderId(ORDER_ID)).hasSize(2);
    }

    @Test
    @DisplayName("활성 결제 조회는 활성 상태 4종만 대상으로 한다")
    void findsActivePaymentUsingActiveStatuses() {
        given(paymentJpaRepository.findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(eq(ORDER_ID), anyCollection()))
            .willReturn(Optional.of(PaymentJpaEntity.from(pendingPayment())));

        assertThat(paymentRepository.findActiveByOrderId(ORDER_ID)).isPresent();

        verify(paymentJpaRepository)
            .findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(ORDER_ID, PaymentStatus.activeStatuses());
    }

    private Payment pendingPayment() {
        return Payment.request(ORDER_ID, 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
    }

}
