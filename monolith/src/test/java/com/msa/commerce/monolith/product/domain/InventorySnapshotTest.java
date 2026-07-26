package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InventorySnapshot 도메인 테스트")
class InventorySnapshotTest {

    private InventorySnapshot snapshot(int available, int reserved) {
        return InventorySnapshot.reconstitute(1L, 1L, null, "MAIN", available, reserved, 10,
            LocalDateTime.now(), 1L);
    }

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("기본값으로 생성 시 위치 MAIN, 임계치 10, 버전 1")
        void createWithDefaults() {
            InventorySnapshot snapshot = InventorySnapshot.create(1L, null, null, null);

            assertThat(snapshot.getLocationCode()).isEqualTo("MAIN");
            assertThat(snapshot.getLowStockThreshold()).isEqualTo(10);
            assertThat(snapshot.getVersion()).isEqualTo(1L);
            assertThat(snapshot.getAvailableQuantity()).isZero();
        }

        @Test
        @DisplayName("productId 없이 생성 시 예외")
        void requiresProductId() {
            assertThatThrownBy(() -> InventorySnapshot.create(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("입출고")
    class StockInOut {

        @Test
        @DisplayName("입고 시 가용 재고가 증가하고 버전이 오른다")
        void stockInIncreasesAvailable() {
            InventorySnapshot snapshot = snapshot(10, 0);

            snapshot.stockIn(5);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(15);
            assertThat(snapshot.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("출고 시 가용 재고가 감소한다")
        void stockOutDecreasesAvailable() {
            InventorySnapshot snapshot = snapshot(10, 0);

            snapshot.stockOut(4);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(6);
        }

        @Test
        @DisplayName("가용 재고보다 많은 출고는 거부된다")
        void stockOutRejectsInsufficient() {
            InventorySnapshot snapshot = snapshot(3, 0);

            assertThatThrownBy(() -> snapshot.stockOut(4))
                .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("0 이하 수량은 거부된다")
        void rejectsNonPositiveQuantity() {
            InventorySnapshot snapshot = snapshot(10, 0);

            assertThatThrownBy(() -> snapshot.stockIn(0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> snapshot.stockOut(-1)).isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("예약")
    class Reservation {

        @Test
        @DisplayName("예약 시 가용→예약으로 이동한다")
        void reserveMovesAvailableToReserved() {
            InventorySnapshot snapshot = snapshot(10, 0);

            snapshot.reserve(3);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(7);
            assertThat(snapshot.getReservedQuantity()).isEqualTo(3);
            assertThat(snapshot.totalQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("가용 재고 초과 예약은 거부된다")
        void reserveRejectsInsufficient() {
            InventorySnapshot snapshot = snapshot(2, 0);

            assertThatThrownBy(() -> snapshot.reserve(3))
                .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("예약 해제 시 예약→가용으로 복귀한다")
        void releaseReturnsToAvailable() {
            InventorySnapshot snapshot = snapshot(7, 3);

            snapshot.releaseReservation(2);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(9);
            assertThat(snapshot.getReservedQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("예약 확정 시 예약만 감소하고 가용은 불변이다")
        void confirmReducesReservedOnly() {
            InventorySnapshot snapshot = snapshot(7, 3);

            snapshot.confirmReservation(3);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(7);
            assertThat(snapshot.getReservedQuantity()).isZero();
        }

        @Test
        @DisplayName("예약량 초과 해제/확정은 거부된다")
        void rejectsOverRelease() {
            InventorySnapshot snapshot = snapshot(7, 1);

            assertThatThrownBy(() -> snapshot.releaseReservation(2))
                .isInstanceOf(InsufficientStockException.class);
            assertThatThrownBy(() -> snapshot.confirmReservation(2))
                .isInstanceOf(InsufficientStockException.class);
        }

    }

    @Nested
    @DisplayName("조정 및 상태")
    class AdjustAndStatus {

        @Test
        @DisplayName("재고 조정은 목표 수량으로 설정된다")
        void adjustSetsTargetQuantity() {
            InventorySnapshot snapshot = snapshot(10, 0);

            snapshot.adjustTo(4);

            assertThat(snapshot.getAvailableQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("음수로 조정은 거부된다")
        void adjustRejectsNegative() {
            InventorySnapshot snapshot = snapshot(10, 0);

            assertThatThrownBy(() -> snapshot.adjustTo(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("재고 상태는 가용 수량과 임계치로 결정된다")
        void stockStatusByThreshold() {
            assertThat(snapshot(0, 0).stockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
            assertThat(snapshot(10, 0).stockStatus()).isEqualTo(StockStatus.LOW_STOCK);
            assertThat(snapshot(11, 0).stockStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

    }

}
