package com.msa.commerce.payment.domain.event;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.payment.domain.Payment;
import com.msa.commerce.payment.domain.PaymentMethod;
import com.msa.commerce.payment.domain.PaymentStatus;

@DisplayName("PaymentResultEvent 계약 테스트")
class PaymentResultEventTest {

    private static final String METADATA = "metadata";

    private static final String CORRELATION_ID = "corr-1";

    /*
     * order-orchestrator 의 PaymentResultEvent 가 선언한 필드 전체.
     * 소비자는 FAIL_ON_UNKNOWN_PROPERTIES 기본값을 쓰므로 이 집합을 벗어나면 역직렬화가 깨진다.
     */
    private static final List<String> CONSUMER_FIELDS = List.of(
        METADATA, "paymentId", "orderId", "customerId", "paymentStatus",
        "amount", "currency", "paymentMethod", "transactionId", "processedAt", "failureReason");

    private static final List<String> CONSUMER_METADATA_FIELDS = List.of(
        "eventId", "correlationId", "timestamp", "eventType", "source", "version");

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    @DisplayName("직렬화 결과의 필드 집합이 소비자 계약과 정확히 일치한다")
    void serializedShapeMatchesConsumerContract() {
        JsonNode json = objectMapper.valueToTree(PaymentResultEvent.from(capturedPayment(), CORRELATION_ID));

        assertThat(fieldNames(json)).containsExactlyInAnyOrderElementsOf(CONSUMER_FIELDS);
        assertThat(fieldNames(json.get(METADATA)))
            .containsExactlyInAnyOrderElementsOf(CONSUMER_METADATA_FIELDS);
    }

    private List<String> fieldNames(JsonNode node) {
        return node.properties().stream().map(Map.Entry::getKey).toList();
    }

    @Test
    @DisplayName("타임스탬프는 ISO-8601 문자열로 직렬화된다")
    void serializesTimestampsAsIsoStrings() {
        JsonNode json = objectMapper.valueToTree(PaymentResultEvent.from(capturedPayment(), CORRELATION_ID));

        assertThat(json.get("processedAt").isTextual()).isTrue();
        assertThat(json.get(METADATA).get("timestamp").isTextual()).isTrue();
    }

    @Test
    @DisplayName("매입된 결제는 SUCCESS 로 발행된다")
    void mapsCapturedToSuccess() {
        Payment payment = capturedPayment();

        PaymentResultEvent event = PaymentResultEvent.from(payment, CORRELATION_ID);

        assertThat(event.paymentStatus()).isEqualTo(PaymentEventStatus.SUCCESS);
        assertThat(event.paymentId()).isEqualTo(payment.getPaymentId());
        assertThat(event.orderId()).isEqualTo(payment.getOrderId());
        assertThat(event.customerId()).isEqualTo(1001L);
        assertThat(event.transactionId()).isEqualTo("TXN-1");
        assertThat(event.paymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(event.failureReason()).isNull();
        assertThat(event.metadata().correlationId()).isEqualTo(CORRELATION_ID);
        assertThat(event.metadata().eventType()).isEqualTo(PaymentResultEvent.EVENT_TYPE);
    }

    @Test
    @DisplayName("실패한 결제는 실패 사유를 함께 싣는다")
    void carriesFailureReason() {
        Payment payment = pendingPayment();
        payment.fail("LIMIT_EXCEEDED", "한도 초과");

        PaymentResultEvent event = PaymentResultEvent.from(payment, null);

        assertThat(event.paymentStatus()).isEqualTo(PaymentEventStatus.FAILED);
        assertThat(event.failureReason()).isEqualTo("한도 초과");
    }

    @Test
    @DisplayName("취소된 결제는 취소 사유를 실패 사유 자리에 싣는다")
    void carriesCancelReason() {
        Payment payment = pendingPayment();
        payment.cancel("고객 변심");

        PaymentResultEvent event = PaymentResultEvent.from(payment, null);

        assertThat(event.paymentStatus()).isEqualTo(PaymentEventStatus.CANCELLED);
        assertThat(event.failureReason()).isEqualTo("고객 변심");
    }

    @Test
    @DisplayName("correlationId 를 주지 않으면 새로 만들어 채운다")
    void generatesCorrelationIdWhenMissing() {
        PaymentResultEvent event = PaymentResultEvent.from(capturedPayment(), null);

        assertThat(event.metadata().correlationId()).isNotBlank();
        assertThat(event.metadata().eventId()).isNotBlank();
    }

    @ParameterizedTest(name = "{0} 은 {1} 로 발행된다")
    @CsvSource({
        "PENDING, PENDING",
        "AUTHORIZED, PENDING",
        "CAPTURED, SUCCESS",
        "PARTIAL_CAPTURED, SUCCESS",
        "PARTIAL_REFUNDED, SUCCESS",
        "FAILED, FAILED",
        "EXPIRED, FAILED",
        "CANCELLED, CANCELLED",
        "REFUNDED, CANCELLED"
    })
    void statusMapping(PaymentStatus internal, PaymentEventStatus published) {
        assertThat(PaymentEventStatus.from(internal)).isEqualTo(published);
    }

    private Payment capturedPayment() {
        Payment payment = pendingPayment();
        payment.capture("EXT-1", "TXN-1", "0001");
        return payment;
    }

    private Payment pendingPayment() {
        return Payment.request(UUID.randomUUID(), 1001L, new BigDecimal("15000.0000"), "KRW",
            PaymentMethod.CREDIT_CARD, "MOCK_PG", null);
    }

}
