package com.msa.commerce.materializedview.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("OrderStatusChangedView 상태 전이 델타 테스트")
class OrderStatusChangedViewTest {

    @Test
    @DisplayName("SHIPPED → DELIVERED: 진행중 -1, 완료 +1")
    void shippedToDelivered() {
        OrderStatusChangedView view = view("SHIPPED", "DELIVERED");

        assertThat(view.hasCategoryChanged()).isTrue();
        assertThat(view.pendingDelta()).isEqualTo(-1);
        assertThat(view.completedDelta()).isEqualTo(1);
        assertThat(view.cancelledDelta()).isZero();
    }

    @Test
    @DisplayName("PENDING → CANCELLED: 진행중 -1, 취소 +1")
    void pendingToCancelled() {
        OrderStatusChangedView view = view("PENDING", "CANCELLED");

        assertThat(view.pendingDelta()).isEqualTo(-1);
        assertThat(view.completedDelta()).isZero();
        assertThat(view.cancelledDelta()).isEqualTo(1);
    }

    @Test
    @DisplayName("DELIVERED → REFUNDED: 완료 -1, 취소 +1")
    void deliveredToRefunded() {
        OrderStatusChangedView view = view("DELIVERED", "REFUNDED");

        assertThat(view.pendingDelta()).isZero();
        assertThat(view.completedDelta()).isEqualTo(-1);
        assertThat(view.cancelledDelta()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"PENDING,CONFIRMED", "CONFIRMED,PAYMENT_PENDING", "PAID,PROCESSING", "PROCESSING,SHIPPED"})
    @DisplayName("진행중 카테고리 내부 전이는 카운터 변화 없음")
    void transitionsWithinActiveCategory(String previous, String current) {
        OrderStatusChangedView view = view(previous, current);

        assertThat(view.hasCategoryChanged()).isFalse();
        assertThat(view.pendingDelta()).isZero();
        assertThat(view.completedDelta()).isZero();
        assertThat(view.cancelledDelta()).isZero();
    }

    @Test
    @DisplayName("알 수 없는 상태 문자열은 진행중으로 분류되어 안전하게 처리")
    void unknownStatusFallsBackToActive() {
        OrderStatusChangedView view = view("PENDING", "SOME_FUTURE_STATUS");

        assertThat(view.hasCategoryChanged()).isFalse();
    }

    private OrderStatusChangedView view(String previous, String current) {
        return new OrderStatusChangedView(
            "event-1", UUID.randomUUID(), 1L, previous, current, LocalDateTime.now());
    }

}
