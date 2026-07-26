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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.payment.adapter.in.web.mapper.PaymentCommandMapper;
import com.msa.commerce.payment.application.port.in.CancelPaymentUseCase;
import com.msa.commerce.payment.application.port.in.ProcessPaymentUseCase;
import com.msa.commerce.payment.application.port.in.RefundPaymentUseCase;
import com.msa.commerce.payment.application.port.in.command.CancelPaymentCommand;
import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.in.command.RefundPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;
import com.msa.commerce.payment.application.service.DuplicatePaymentException;
import com.msa.commerce.payment.application.service.PaymentNotFoundException;
import com.msa.commerce.payment.domain.InvalidPaymentStateException;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommandController 테스트")
class PaymentCommandControllerTest {

    private static final String PAYMENTS_URL = "/api/v1/payments";

    private static final String CANCEL_URL = "/api/v1/payments/{paymentId}/cancel";

    private static final String REFUND_URL = "/api/v1/payments/{paymentId}/refund";

    private static final String JSON_STATUS = "$.status";

    private static final String JSON_CODE = "$.code";

    private static final String CANCEL_BODY = """
        { "reason": "고객 변심" }
        """;

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

    @Mock
    private CancelPaymentUseCase cancelPaymentUseCase;

    @Mock
    private RefundPaymentUseCase refundPaymentUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new PaymentCommandController(processPaymentUseCase, cancelPaymentUseCase,
                refundPaymentUseCase, new PaymentCommandMapper()))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
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

    @Nested
    @DisplayName("결제 요청")
    class Process {

        @Test
        @DisplayName("성공하면 201 과 Location 헤더를 반환한다")
        void processPayment() throws Exception {
            given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .willReturn(response(PaymentStatus.CAPTURED));

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PROCESS_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/" + PAYMENT_ID))
                .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID.toString()))
                .andExpect(jsonPath(JSON_STATUS).value("CAPTURED"));
        }

        @Test
        @DisplayName("통화를 생략하면 KRW 로 채워 유스케이스에 전달한다")
        void defaultsCurrencyToKrw() throws Exception {
            given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .willReturn(response(PaymentStatus.CAPTURED));

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PROCESS_BODY))
                .andExpect(status().isCreated());

            then(processPaymentUseCase).should().process(argThat(command -> "KRW".equals(command.currency())));
        }

        @Test
        @DisplayName("X-Correlation-Id 헤더를 커맨드로 전달한다")
        void propagatesCorrelationId() throws Exception {
            given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .willReturn(response(PaymentStatus.CAPTURED));

            mockMvc.perform(post(PAYMENTS_URL)
                    .header("X-Correlation-Id", "corr-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PROCESS_BODY))
                .andExpect(status().isCreated());

            then(processPaymentUseCase).should().process(argThat(command -> "corr-1".equals(command.correlationId())));
        }

        @Test
        @DisplayName("PG사가 거절해도 결제 기록은 생성되므로 201 로 응답한다")
        void returnsCreatedEvenWhenDeclined() throws Exception {
            given(processPaymentUseCase.process(any(ProcessPaymentCommand.class)))
                .willReturn(response(PaymentStatus.FAILED));

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PROCESS_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(JSON_STATUS).value("FAILED"));
        }

        @Test
        @DisplayName("필수 값이 빠지면 400 을 반환한다")
        void rejectsMissingRequiredFields() throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
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
            mockMvc.perform(post(PAYMENTS_URL)
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

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(PROCESS_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(JSON_CODE).value("PY1002"));
        }

    }

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("취소에 성공하면 200 과 CANCELLED 상태를 반환한다")
        void cancelPayment() throws Exception {
            given(cancelPaymentUseCase.cancel(any(CancelPaymentCommand.class)))
                .willReturn(response(PaymentStatus.CANCELLED));

            mockMvc.perform(post(CANCEL_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CANCEL_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_STATUS).value("CANCELLED"));

            then(cancelPaymentUseCase).should().cancel(argThat(command ->
                PAYMENT_ID.equals(command.paymentId()) && "고객 변심".equals(command.reason())));
        }

        @Test
        @DisplayName("취소 사유가 없으면 400 을 반환한다")
        void rejectsMissingReason() throws Exception {
            mockMvc.perform(post(CANCEL_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 결제를 취소하면 404 를 반환한다")
        void returnsNotFound() throws Exception {
            given(cancelPaymentUseCase.cancel(any(CancelPaymentCommand.class)))
                .willThrow(new PaymentNotFoundException("Payment not found with ID: " + PAYMENT_ID));

            mockMvc.perform(post(CANCEL_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CANCEL_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(JSON_CODE).value("PY1001"));
        }

        @Test
        @DisplayName("이미 매입된 결제를 취소하면 400 을 반환한다")
        void returnsBadRequestForCapturedPayment() throws Exception {
            given(cancelPaymentUseCase.cancel(any(CancelPaymentCommand.class)))
                .willThrow(new InvalidPaymentStateException("Payment in CAPTURED cannot be cancelled"));

            mockMvc.perform(post(CANCEL_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(CANCEL_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(JSON_CODE).value("PY1003"));
        }

    }

    @Nested
    @DisplayName("결제 환불")
    class Refund {

        @Test
        @DisplayName("전액 환불에 성공하면 200 과 REFUNDED 상태를 반환한다")
        void refundPayment() throws Exception {
            given(refundPaymentUseCase.refund(any(RefundPaymentCommand.class)))
                .willReturn(response(PaymentStatus.REFUNDED));

            mockMvc.perform(post(REFUND_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "reason": "상품 불량" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_STATUS).value("REFUNDED"));

            then(refundPaymentUseCase).should().refund(argThat(command -> command.amount() == null));
        }

        @Test
        @DisplayName("부분 환불 금액을 커맨드로 전달한다")
        void refundPartially() throws Exception {
            given(refundPaymentUseCase.refund(any(RefundPaymentCommand.class)))
                .willReturn(response(PaymentStatus.PARTIAL_REFUNDED));

            mockMvc.perform(post(REFUND_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "amount": 5000.0000, "reason": "일부 반품" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_STATUS).value("PARTIAL_REFUNDED"));

            then(refundPaymentUseCase).should().refund(argThat(command ->
                command.amount().compareTo(new BigDecimal("5000.0000")) == 0));
        }

        @Test
        @DisplayName("환불 금액이 0 이하면 400 을 반환한다")
        void rejectsNonPositiveAmount() throws Exception {
            mockMvc.perform(post(REFUND_URL, PAYMENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "amount": 0, "reason": "일부 반품" }
                        """))
                .andExpect(status().isBadRequest());
        }

    }

}
