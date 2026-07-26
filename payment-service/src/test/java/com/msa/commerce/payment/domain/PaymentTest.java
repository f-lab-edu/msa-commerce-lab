package com.msa.commerce.payment.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Payment 도메인 테스트")
class PaymentTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    private static final Long CUSTOMER_ID = 1001L;

    private static final BigDecimal AMOUNT = new BigDecimal("15000.0000");

    private static final String PROVIDER = "MOCK_PG";

    private static Payment requestPayment(BigDecimal amount, PaymentMethod method) {
        return Payment.request(ORDER_ID, CUSTOMER_ID, amount, "KRW", method, PROVIDER, null);
    }

    @Test
    @DisplayName("동일성은 paymentId 로 판단한다")
    void equalsByPaymentId() {
        Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
        Payment same = Payment.builder().paymentId(payment.getPaymentId()).build();
        Payment other = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

        assertThat(payment).isEqualTo(same).isNotEqualTo(other);
        assertThat(payment).hasSameHashCodeAs(same);
    }

    @Nested
    @DisplayName("결제 요청 생성")
    class Request {

        @Test
        @DisplayName("요청 시 PENDING 상태의 결제가 생성된다")
        void createsPendingPayment() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            assertThat(payment.getPaymentId()).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(payment.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(payment.getAmount()).isEqualTo(AMOUNT);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getPaymentProvider()).isEqualTo(PROVIDER);
            assertThat(payment.getVersion()).isEqualTo(1L);
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("통화를 지정하지 않으면 KRW 로 채워진다")
        void defaultsCurrencyToKrw() {
            Payment payment = Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, null,
                PaymentMethod.CREDIT_CARD, PROVIDER, null);

            assertThat(payment.getCurrency()).isEqualTo("KRW");
        }

        @Test
        @DisplayName("통화 코드는 대문자로 정규화된다")
        void normalizesCurrencyToUpperCase() {
            Payment payment = Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "usd",
                PaymentMethod.CREDIT_CARD, PROVIDER, null);

            assertThat(payment.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("paymentDetails 는 방어적으로 복사되어 외부 변경에 영향받지 않는다")
        void copiesPaymentDetails() {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("cardLast4", "1234");

            Payment payment = Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, details);
            details.put("cardLast4", "9999");

            assertThat(payment.getPaymentDetails()).containsEntry("cardLast4", "1234");
        }

        @Test
        @DisplayName("반환된 paymentDetails 는 수정할 수 없다")
        void paymentDetailsIsUnmodifiable() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            assertThatThrownBy(() -> payment.getPaymentDetails().put("key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("주문 ID 가 없으면 생성할 수 없다")
        void rejectsMissingOrderId() {
            assertThatThrownBy(() -> Payment.request(null, CUSTOMER_ID, AMOUNT, "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order ID");
        }

        @Test
        @DisplayName("고객 ID 가 없으면 생성할 수 없다")
        void rejectsMissingCustomerId() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, null, AMOUNT, "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer ID");
        }

        @Test
        @DisplayName("금액이 0 이하면 생성할 수 없다")
        void rejectsNonPositiveAmount() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, CUSTOMER_ID, BigDecimal.ZERO, "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("금액이 컬럼 정밀도를 넘으면 생성할 수 없다")
        void rejectsAmountOverMax() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, CUSTOMER_ID, new BigDecimal("100000000.0000"), "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
        }

        @Test
        @DisplayName("통화 코드 길이가 3이 아니면 생성할 수 없다")
        void rejectsInvalidCurrencyLength() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "KRWW",
                PaymentMethod.CREDIT_CARD, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISO 4217");
        }

        @Test
        @DisplayName("결제 수단이 없으면 생성할 수 없다")
        void rejectsMissingPaymentMethod() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "KRW",
                null, PROVIDER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment method");
        }

        @Test
        @DisplayName("PG사 정보가 비어 있으면 생성할 수 없다")
        void rejectsBlankProvider() {
            assertThatThrownBy(() -> Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "KRW",
                PaymentMethod.CREDIT_CARD, "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment provider");
        }

    }

    @Nested
    @DisplayName("상태 전이")
    class Transition {

        @Test
        @DisplayName("승인하면 AUTHORIZED 가 되고 PG 정보와 승인 시각이 기록된다")
        void authorize() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.VIRTUAL_ACCOUNT);

            payment.authorize("EXT-1", "TXN-1", "0001");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(payment.getExternalPaymentId()).isEqualTo("EXT-1");
            assertThat(payment.getGatewayTransactionId()).isEqualTo("TXN-1");
            assertThat(payment.getApprovalNumber()).isEqualTo("0001");
            assertThat(payment.getAuthorizedAt()).isNotNull();
            assertThat(payment.getCapturedAt()).isNull();
            assertThat(payment.isSettled()).isFalse();
        }

        @Test
        @DisplayName("PENDING 에서 바로 매입하면 승인 시각도 함께 기록된다")
        void captureFromPendingAlsoRecordsAuthorizedAt() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            payment.capture("EXT-1", "TXN-1", "0001");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
            assertThat(payment.getAuthorizedAt()).isNotNull();
            assertThat(payment.getCapturedAt()).isNotNull();
            assertThat(payment.isSettled()).isTrue();
        }

        @Test
        @DisplayName("승인 후 매입하면 최초 승인 시각이 보존된다")
        void captureAfterAuthorizeKeepsAuthorizedAt() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.VIRTUAL_ACCOUNT);
            payment.authorize("EXT-1", "TXN-1", "0001");
            var authorizedAt = payment.getAuthorizedAt();

            payment.capture(null, null, null);

            assertThat(payment.getAuthorizedAt()).isEqualTo(authorizedAt);
            assertThat(payment.getExternalPaymentId()).isEqualTo("EXT-1");
            assertThat(payment.getGatewayTransactionId()).isEqualTo("TXN-1");
            assertThat(payment.getApprovalNumber()).isEqualTo("0001");
        }

        @Test
        @DisplayName("실패하면 FAILED 가 되고 실패 사유가 기록된다")
        void fail() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            payment.fail("LIMIT_EXCEEDED", "한도 초과");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("LIMIT_EXCEEDED");
            assertThat(payment.getFailureReason()).isEqualTo("한도 초과");
            assertThat(payment.getFailedAt()).isNotNull();
        }

        @Test
        @DisplayName("만료시키면 EXPIRED 가 된다")
        void expire() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.VIRTUAL_ACCOUNT);

            payment.expire();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        }

        @Test
        @DisplayName("이미 매입된 결제는 다시 실패 처리할 수 없다")
        void rejectsInvalidTransition() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.capture("EXT-1", "TXN-1", "0001");

            assertThatThrownBy(() -> payment.fail("X", "Y"))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("CAPTURED")
                .hasMessageContaining("FAILED");
        }

        @Test
        @DisplayName("실패한 결제는 다시 승인할 수 없다")
        void rejectsAuthorizeAfterFail() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.fail("DECLINED", "카드 오류");

            assertThatThrownBy(() -> payment.authorize("EXT-1", "TXN-1", "0001"))
                .isInstanceOf(InvalidPaymentStateException.class);
        }

    }

}
