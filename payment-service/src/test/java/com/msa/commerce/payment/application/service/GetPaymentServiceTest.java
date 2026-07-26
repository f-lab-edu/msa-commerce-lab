package com.msa.commerce.payment.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.port.out.PaymentRepository;
import com.msa.commerce.payment.application.service.mapper.PaymentResponseMapper;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPaymentService 단위 테스트")
class GetPaymentServiceTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    private PaymentRepository paymentRepository;

    private GetPaymentService getPaymentService;

    @BeforeEach
    void setUp() {
        getPaymentService = new GetPaymentService(paymentRepository, new PaymentResponseMapper());
    }

    @Test
    @DisplayName("결제 ID 로 조회하면 결제 정보를 반환한다")
    void getByPaymentId() {
        Payment payment = payment();
        given(paymentRepository.findByPaymentId(payment.getPaymentId())).willReturn(Optional.of(payment));

        PaymentResponse response = getPaymentService.getByPaymentId(payment.getPaymentId());

        assertThat(response.paymentId()).isEqualTo(payment.getPaymentId());
        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 결제 ID 로 조회하면 예외가 발생한다")
    void getByPaymentIdNotFound() {
        UUID missing = UUID.randomUUID();
        given(paymentRepository.findByPaymentId(missing)).willReturn(Optional.empty());

        assertThatThrownBy(() -> getPaymentService.getByPaymentId(missing))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining(missing.toString());
    }

    @Test
    @DisplayName("주문 ID 로 조회하면 해당 주문의 결제 이력을 모두 반환한다")
    void getByOrderId() {
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(List.of(payment(), payment()));

        List<PaymentResponse> responses = getPaymentService.getByOrderId(ORDER_ID);

        assertThat(responses).hasSize(2)
            .allSatisfy(response -> assertThat(response.orderId()).isEqualTo(ORDER_ID));
    }

    @Test
    @DisplayName("결제 이력이 없는 주문은 빈 목록을 반환한다")
    void getByOrderIdEmpty() {
        given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(List.of());

        assertThat(getPaymentService.getByOrderId(ORDER_ID)).isEmpty();
    }

    private Payment payment() {
        return Payment.request(ORDER_ID, 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
    }

}
