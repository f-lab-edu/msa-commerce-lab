package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InventoryEvent 도메인 테스트")
class InventoryEventTest {

    @Test
    @DisplayName("스냅샷 변경 전/후로 이벤트가 기록된다")
    void recordsChangeFromSnapshot() {
        InventorySnapshot snapshot = InventorySnapshot.reconstitute(
            1L, 100L, 5L, "MAIN", 10, 0, 10, LocalDateTime.now(), 1L);
        int before = snapshot.getAvailableQuantity();
        snapshot.stockIn(5);

        InventoryEvent event = InventoryEvent.record(InventoryEventType.STOCK_IN, snapshot, before,
            InventoryEvent.ChangeContext.of("정기 입고"));

        assertThat(event.getQuantityBefore()).isEqualTo(10);
        assertThat(event.getQuantityAfter()).isEqualTo(15);
        assertThat(event.getQuantityChange()).isEqualTo(5);
        assertThat(event.getAggregateId()).isEqualTo("PRODUCT_100_V5_MAIN");
        assertThat(event.getAggregateVersion()).isEqualTo(snapshot.getVersion());
        assertThat(event.getChangeReason()).isEqualTo("정기 입고");
        assertThat(event.getCorrelationId()).isNotBlank();
    }

    @Test
    @DisplayName("변형상품 없는 스냅샷의 aggregateId는 V NA로 표기된다")
    void aggregateIdWithoutVariant() {
        InventorySnapshot snapshot = InventorySnapshot.reconstitute(
            1L, 100L, null, "WAREHOUSE-1", 10, 0, 10, LocalDateTime.now(), 1L);
        snapshot.stockOut(3);

        InventoryEvent event = InventoryEvent.record(InventoryEventType.STOCK_OUT, snapshot, 10,
            InventoryEvent.ChangeContext.builder().reason("출고").referenceType("ORDER").referenceId("ORD-1").build());

        assertThat(event.getAggregateId()).isEqualTo("PRODUCT_100_VNA_WAREHOUSE-1");
        assertThat(event.getQuantityChange()).isEqualTo(-3);
        assertThat(event.getReferenceType()).isEqualTo("ORDER");
        assertThat(event.getReferenceId()).isEqualTo("ORD-1");
    }

}
