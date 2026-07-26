package com.msa.commerce.payment.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.payment.adapter.in.web.mapper.PaymentCommandMapper;
import com.msa.commerce.payment.application.port.in.ProcessPaymentUseCase;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.service.DuplicatePaymentException;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommandController 테스트")
class PaymentCommandControllerTest {

    private static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID PAYMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final String PROCESS_BODY = """
        {
          "orderId": "11111111-1111-1111-1111-111111111111",
          "customerId": 1001,
          "amount": 15000.0000,
          "paymentMethod": "CREDIT_CARD"
        }
        """;

    private MockMvc mockMvc;

    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PaymentCommandController(processPaymentUseCase, new PaymentCommandMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("결제 요청에 성공하면 201 과 Location 헤더를 반환한다")
    void processPayment() throws Exception {
        given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
            .willReturn(response(PaymentStatus.CAPTURED));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROCESS_BODY))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/v1/payments/" + PAYMENT_ID))
            .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID.toString()))
            .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    @DisplayName("통화를 생략하면 KRW 로 채워 유스케이스에 전달한다")
    void defaultsCurrencyToKrw() throws Exception {
        given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
            .willReturn(response(PaymentStatus.CAPTURED));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROCESS_BODY))
            .andExpect(status().isCreated());

        then(processPaymentUseCase).should().process(argThat(command -> "KRW".equals(command.currency())));
    }

    @Test
    @DisplayName("PG사가 거절해도 결제 기록은 생성되므로 201 로 응답한다")
    void returnsCreatedEvenWhenDeclined() throws Exception {
        given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
            .willReturn(response(PaymentStatus.FAILED));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROCESS_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    @DisplayName("필수 값이 빠지면 400 을 반환한다")
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerId": 1001,
                      "amount": 15000.0000
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("금액이 0 이하면 400 을 반환한다")
    void rejectsNonPositiveAmount() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "orderId": "11111111-1111-1111-1111-111111111111",
                      "customerId": 1001,
                      "amount": 0,
                      "paymentMethod": "CREDIT_CARD"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("진행 중인 결제가 있으면 409 를 반환한다")
    void returnsConflictOnDuplicate() throws Exception {
        given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
            .willThrow(new DuplicatePaymentException(ORDER_ID));

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PROCESS_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PY1002"));
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
