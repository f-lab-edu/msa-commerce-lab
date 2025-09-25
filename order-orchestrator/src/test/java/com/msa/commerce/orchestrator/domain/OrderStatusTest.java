package com.msa.commerce.orchestrator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderStatus 열거형 테스트")
class OrderStatusTest {

    @Test
    @DisplayName("취소 가능한 상태 확인")
    void canBeCancelled_Success() {
        // when & then
        assertThat(OrderStatus.PENDING.canBeCancelled()).isTrue();
        assertThat(OrderStatus.CONFIRMED.canBeCancelled()).isTrue();
        assertThat(OrderStatus.PAYMENT_PENDING.canBeCancelled()).isTrue();

        assertThat(OrderStatus.PAID.canBeCancelled()).isFalse();
        assertThat(OrderStatus.PROCESSING.canBeCancelled()).isFalse();
        assertThat(OrderStatus.SHIPPED.canBeCancelled()).isFalse();
        assertThat(OrderStatus.DELIVERED.canBeCancelled()).isFalse();
        assertThat(OrderStatus.CANCELLED.canBeCancelled()).isFalse();
        assertThat(OrderStatus.REFUNDED.canBeCancelled()).isFalse();
        assertThat(OrderStatus.FAILED.canBeCancelled()).isFalse();
    }

    @Test
    @DisplayName("완료된 상태 확인")
    void isCompleted_Success() {
        // when & then
        assertThat(OrderStatus.DELIVERED.isCompleted()).isTrue();
        assertThat(OrderStatus.CANCELLED.isCompleted()).isTrue();
        assertThat(OrderStatus.REFUNDED.isCompleted()).isTrue();
        assertThat(OrderStatus.FAILED.isCompleted()).isTrue();

        assertThat(OrderStatus.PENDING.isCompleted()).isFalse();
        assertThat(OrderStatus.CONFIRMED.isCompleted()).isFalse();
        assertThat(OrderStatus.PAYMENT_PENDING.isCompleted()).isFalse();
        assertThat(OrderStatus.PAID.isCompleted()).isFalse();
        assertThat(OrderStatus.PROCESSING.isCompleted()).isFalse();
        assertThat(OrderStatus.SHIPPED.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("처리 중인 상태 확인")
    void isProcessing_Success() {
        // when & then
        assertThat(OrderStatus.PAID.isProcessing()).isTrue();
        assertThat(OrderStatus.PROCESSING.isProcessing()).isTrue();
        assertThat(OrderStatus.SHIPPED.isProcessing()).isTrue();

        assertThat(OrderStatus.PENDING.isProcessing()).isFalse();
        assertThat(OrderStatus.CONFIRMED.isProcessing()).isFalse();
        assertThat(OrderStatus.PAYMENT_PENDING.isProcessing()).isFalse();
        assertThat(OrderStatus.DELIVERED.isProcessing()).isFalse();
        assertThat(OrderStatus.CANCELLED.isProcessing()).isFalse();
        assertThat(OrderStatus.REFUNDED.isProcessing()).isFalse();
        assertThat(OrderStatus.FAILED.isProcessing()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("모든 상태값이 정의되어 있는지 확인")
    void allStatusValuesDefined(OrderStatus status) {
        // then
        assertThat(status).isNotNull();
        assertThat(status.name()).isNotBlank();
    }

    @Test
    @DisplayName("상태별 논리적 일관성 확인")
    void statusLogicalConsistency() {
        // given & when & then
        for (OrderStatus status : OrderStatus.values()) {
            // 완료된 상태는 처리 중이거나 취소 가능하면 안됨
            if (status.isCompleted()) {
                assertThat(status.isProcessing())
                    .as("완료된 상태 %s는 처리 중이면 안됨", status)
                    .isFalse();

                if (status != OrderStatus.CANCELLED) {
                    assertThat(status.canBeCancelled())
                        .as("완료된 상태 %s는 취소 가능하면 안됨 (CANCELLED 제외)", status)
                        .isFalse();
                }
            }
        }
    }
}