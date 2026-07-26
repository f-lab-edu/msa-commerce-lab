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

    private static final String CARD_LAST4_KEY = "cardLast4";

    private static final String EXTERNAL_ID = "EXT-1";

    private static final String TRANSACTION_ID = "TXN-1";

    private static final String APPROVAL_NUMBER = "0001";

    private static final BigDecimal PARTIAL_AMOUNT = new BigDecimal("5000.0000");

    private static final BigDecimal REMAINING_AMOUNT = new BigDecimal("10000.0000");

    private static final String CANCEL_REASON = "고객 변심";

    private static final String REFUND_REASON = "상품 불량";

    private static final String PARTIAL_REFUND_REASON = "일부 반품";

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
            details.put(CARD_LAST4_KEY, "1234");

            Payment payment = Payment.request(ORDER_ID, CUSTOMER_ID, AMOUNT, "KRW",
                PaymentMethod.CREDIT_CARD, PROVIDER, details);
            details.put(CARD_LAST4_KEY, "9999");

            assertThat(payment.getPaymentDetails()).containsEntry(CARD_LAST4_KEY, "1234");
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

            payment.authorize(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
            assertThat(payment.getExternalPaymentId()).isEqualTo(EXTERNAL_ID);
            assertThat(payment.getGatewayTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(payment.getApprovalNumber()).isEqualTo(APPROVAL_NUMBER);
            assertThat(payment.getAuthorizedAt()).isNotNull();
            assertThat(payment.getCapturedAt()).isNull();
            assertThat(payment.isSettled()).isFalse();
        }

        @Test
        @DisplayName("PENDING 에서 바로 매입하면 승인 시각도 함께 기록된다")
        void captureFromPendingAlsoRecordsAuthorizedAt() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            payment.capture(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
            assertThat(payment.getAuthorizedAt()).isNotNull();
            assertThat(payment.getCapturedAt()).isNotNull();
            assertThat(payment.isSettled()).isTrue();
        }

        @Test
        @DisplayName("승인 후 매입하면 최초 승인 시각이 보존된다")
        void captureAfterAuthorizeKeepsAuthorizedAt() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.VIRTUAL_ACCOUNT);
            payment.authorize(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);
            var authorizedAt = payment.getAuthorizedAt();

            payment.capture(null, null, null);

            assertThat(payment.getAuthorizedAt()).isEqualTo(authorizedAt);
            assertThat(payment.getExternalPaymentId()).isEqualTo(EXTERNAL_ID);
            assertThat(payment.getGatewayTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(payment.getApprovalNumber()).isEqualTo(APPROVAL_NUMBER);
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
            payment.capture(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);

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

            assertThatThrownBy(() -> payment.authorize(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER))
                .isInstanceOf(InvalidPaymentStateException.class);
        }

    }

    @Nested
    @DisplayName("취소")
    class Cancel {

        @Test
        @DisplayName("승인 상태에서 취소하면 CANCELLED 가 되고 사유가 남는다")
        void cancelAuthorized() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.VIRTUAL_ACCOUNT);
            payment.authorize(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);

            payment.cancel(CANCEL_REASON);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getCancelReason()).isEqualTo(CANCEL_REASON);
            assertThat(payment.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 상태에서도 취소할 수 있다")
        void cancelPending() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            payment.cancel("주문 취소");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 매입된 결제는 취소 대신 환불해야 한다")
        void rejectsCancelAfterCapture() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.capture(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);

            assertThatThrownBy(() -> payment.cancel(CANCEL_REASON))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("refund it instead");
        }

    }

    @Nested
    @DisplayName("환불")
    class Refund {

        @Test
        @DisplayName("전액 환불하면 REFUNDED 가 된다")
        void fullRefund() {
            Payment payment = capturedPayment();

            payment.refund(AMOUNT, REFUND_REASON);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundAmount()).isEqualByComparingTo(AMOUNT);
            assertThat(payment.getRefundReason()).isEqualTo(REFUND_REASON);
            assertThat(payment.getRefundedAt()).isNotNull();
            assertThat(payment.refundableAmount()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("부분 환불하면 PARTIAL_REFUNDED 가 되고 잔여 금액이 남는다")
        void partialRefund() {
            Payment payment = capturedPayment();

            payment.refund(PARTIAL_AMOUNT, PARTIAL_REFUND_REASON);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
            assertThat(payment.getRefundAmount()).isEqualByComparingTo("5000.0000");
            assertThat(payment.refundableAmount()).isEqualByComparingTo("10000.0000");
        }

        @Test
        @DisplayName("부분 환불을 누적해 전액에 도달하면 REFUNDED 가 된다")
        void accumulatesPartialRefunds() {
            Payment payment = capturedPayment();

            payment.refund(PARTIAL_AMOUNT, PARTIAL_REFUND_REASON);
            payment.refund(REMAINING_AMOUNT, "잔여 반품");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundAmount()).isEqualByComparingTo(AMOUNT);
        }

        @Test
        @DisplayName("잔여 금액을 넘는 환불은 거부한다")
        void rejectsOverRefund() {
            Payment payment = capturedPayment();
            payment.refund(REMAINING_AMOUNT, PARTIAL_REFUND_REASON);

            assertThatThrownBy(() -> payment.refund(REMAINING_AMOUNT, "추가 반품"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds the refundable amount");
        }

        @Test
        @DisplayName("0 이하 금액은 환불할 수 없다")
        void rejectsNonPositiveRefund() {
            Payment payment = capturedPayment();

            assertThatThrownBy(() -> payment.refund(BigDecimal.ZERO, "잘못된 요청"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");
        }

        @Test
        @DisplayName("매입되지 않은 결제는 환불할 수 없다")
        void rejectsRefundBeforeCapture() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);

            assertThatThrownBy(() -> payment.refund(AMOUNT, REFUND_REASON))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("cannot be refunded");
        }

        @Test
        @DisplayName("환불 전에는 결제 금액 전액이 환불 가능하다")
        void refundableAmountStartsAtFullAmount() {
            assertThat(capturedPayment().refundableAmount()).isEqualByComparingTo(AMOUNT);
        }

        private Payment capturedPayment() {
            Payment payment = requestPayment(AMOUNT, PaymentMethod.CREDIT_CARD);
            payment.capture(EXTERNAL_ID, TRANSACTION_ID, APPROVAL_NUMBER);
            return payment;
        }

    }

}
