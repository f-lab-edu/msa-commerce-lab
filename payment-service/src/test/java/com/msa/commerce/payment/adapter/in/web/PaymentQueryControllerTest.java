package com.msa.commerce.payment.adapter.in.web;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.payment.application.port.in.GetPaymentUseCase;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.service.PaymentNotFoundException;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentQueryController 테스트")
class PaymentQueryControllerTest {

    private static final String PAYMENT_URL = "/api/v1/payments/{paymentId}";

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID PAYMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private MockMvc mockMvc;

    @Mock
    private GetPaymentUseCase getPaymentUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PaymentQueryController(getPaymentUseCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("결제 ID 로 조회하면 200 과 결제 정보를 반환한다")
    void getPayment() throws Exception {
        given(getPaymentUseCase.getByPaymentId(PAYMENT_ID)).willReturn(response(PaymentStatus.CAPTURED));

        mockMvc.perform(get(PAYMENT_URL, PAYMENT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID.toString()))
            .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
            .andExpect(jsonPath("$.status").value("CAPTURED"))
            .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    @DisplayName("존재하지 않는 결제를 조회하면 404 를 반환한다")
    void getPaymentNotFound() throws Exception {
        given(getPaymentUseCase.getByPaymentId(PAYMENT_ID))
            .willThrow(new PaymentNotFoundException("Payment not found with ID: " + PAYMENT_ID));

        mockMvc.perform(get(PAYMENT_URL, PAYMENT_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("PY1001"));
    }

    @Test
    @DisplayName("결제 ID 형식이 잘못되면 400 을 반환한다")
    void getPaymentWithMalformedId() throws Exception {
        mockMvc.perform(get(PAYMENT_URL, "not-a-uuid"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 ID 로 조회하면 결제 이력 목록을 반환한다")
    void getPaymentsByOrder() throws Exception {
        given(getPaymentUseCase.getByOrderId(ORDER_ID))
            .willReturn(List.of(response(PaymentStatus.FAILED), response(PaymentStatus.CAPTURED)));

        mockMvc.perform(get("/api/v1/payments/orders/{orderId}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].status").value("FAILED"))
            .andExpect(jsonPath("$[1].status").value("CAPTURED"));
    }

    @Test
    @DisplayName("결제 이력이 없는 주문은 빈 배열을 반환한다")
    void getPaymentsByOrderEmpty() throws Exception {
        given(getPaymentUseCase.getByOrderId(ORDER_ID)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/orders/{orderId}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    private PaymentResponse response(PaymentStatus status) {
        return PaymentResponse.builder()
            .paymentId(PAYMENT_ID)
            .orderId(ORDER_ID)
            .customerId(1001L)
            .amount(new BigDecimal("15000.0000"))
            .currency("KRW")
            .status(status)
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .paymentProvider("MOCK_PG")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

}
