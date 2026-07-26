package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventoryEventType;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;

@DataJpaTest
@Import({InventoryRepositoryAdapter.class, InventoryMapperImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("InventoryRepositoryAdapter 슬라이스 테스트")
class InventoryRepositoryAdapterTest {

    @Autowired
    private InventoryRepositoryAdapter adapter;

    @Test
    @DisplayName("스냅샷 저장 후 위치 조건으로 조회된다")
    void savesAndFindsSnapshot() {
        InventorySnapshot snapshot = InventorySnapshot.create(100L, null, "MAIN", 5);
        snapshot.stockIn(20);

        adapter.save(snapshot);

        Optional<InventorySnapshot> found = adapter.findOne(100L, null, "MAIN");
        assertThat(found).isPresent();
        assertThat(found.get().getAvailableQuantity()).isEqualTo(20);
        assertThat(found.get().getLowStockThreshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("variantId가 다른 스냅샷은 조회되지 않는다")
    void distinguishesVariantRows() {
        InventorySnapshot base = InventorySnapshot.create(200L, null, "MAIN", null);
        base.stockIn(10);
        adapter.save(base);

        assertThat(adapter.findOne(200L, 1L, "MAIN")).isEmpty();
        assertThat(adapter.findOne(200L, null, "MAIN")).isPresent();
    }

    @Test
    @DisplayName("비관적 락 조회도 동일 조건으로 동작한다")
    void findForUpdateReturnsRow() {
        InventorySnapshot snapshot = InventorySnapshot.create(300L, 7L, "WH1", null);
        snapshot.stockIn(3);
        adapter.save(snapshot);

        assertThat(adapter.findForUpdate(300L, 7L, "WH1")).isPresent();
    }

    @Test
    @DisplayName("이벤트 적재 후 기간 조건으로 이력이 조회된다")
    void appendsAndFindsEvents() {
        InventorySnapshot snapshot = InventorySnapshot.create(400L, null, "MAIN", null);
        int before = snapshot.getAvailableQuantity();
        snapshot.stockIn(15);
        InventorySnapshot saved = adapter.save(snapshot);

        adapter.appendEvent(InventoryEvent.record(InventoryEventType.STOCK_IN, saved, before,
            InventoryEvent.ChangeContext.of("입고")));

        List<InventoryEvent> events = adapter.findEventsByProductId(400L,
            LocalDateTime.now().minusMinutes(1));
        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getEventType()).isEqualTo(InventoryEventType.STOCK_IN);
        assertThat(events.getFirst().getQuantityChange()).isEqualTo(15);
    }

    @Test
    @DisplayName("productId 기준 전체 스냅샷 조회")
    void findsAllSnapshotsByProduct() {
        InventorySnapshot main = InventorySnapshot.create(500L, null, "MAIN", null);
        main.stockIn(1);
        InventorySnapshot warehouse = InventorySnapshot.create(500L, null, "WH1", null);
        warehouse.stockIn(2);
        adapter.save(main);
        adapter.save(warehouse);

        assertThat(adapter.findByProductId(500L)).hasSize(2);
    }

}
