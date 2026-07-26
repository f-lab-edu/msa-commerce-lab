package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.InventoryChangeCommand;
import com.msa.commerce.monolith.product.application.port.out.InventoryRepository;
import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventoryEventType;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;
import com.msa.commerce.monolith.product.domain.StockStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService 단위 테스트")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("스냅샷이 없으면 입고 시 새로 생성한다")
    void stockInCreatesSnapshotWhenMissing() {
        when(inventoryRepository.findForUpdate(1L, null, "MAIN")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventorySnapshot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.stockIn(command(10));

        ArgumentCaptor<InventorySnapshot> captor = ArgumentCaptor.forClass(InventorySnapshot.class);
        verify(inventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("연산 후 스냅샷 저장과 이벤트 적재가 함께 수행된다")
    void savesSnapshotAndAppendsEvent() {
        when(inventoryRepository.findForUpdate(1L, null, "MAIN"))
            .thenReturn(Optional.of(snapshot(10, 0)));
        when(inventoryRepository.save(any(InventorySnapshot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.stockOut(command(4));

        ArgumentCaptor<InventoryEvent> captor = ArgumentCaptor.forClass(InventoryEvent.class);
        verify(inventoryRepository).appendEvent(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(InventoryEventType.STOCK_OUT);
        assertThat(captor.getValue().getQuantityChange()).isEqualTo(-4);
    }

    @Test
    @DisplayName("입고 외 연산은 스냅샷이 없으면 실패한다")
    void nonStockInRequiresExistingSnapshot() {
        when(inventoryRepository.findForUpdate(1L, null, "MAIN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserve(command(1)))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryRepository, never()).save(any(InventorySnapshot.class));
    }

    @Test
    @DisplayName("예약-해제-확정 흐름이 이벤트 타입별로 기록된다")
    void reservationFlowRecordsEventTypes() {
        when(inventoryRepository.findForUpdate(1L, null, "MAIN"))
            .thenReturn(Optional.of(snapshot(10, 5)));
        when(inventoryRepository.save(any(InventorySnapshot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.releaseReservation(command(2));
        inventoryService.confirmReservation(command(2));

        ArgumentCaptor<InventoryEvent> captor = ArgumentCaptor.forClass(InventoryEvent.class);
        verify(inventoryRepository, times(2)).appendEvent(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(InventoryEvent::getEventType)
            .containsExactly(InventoryEventType.STOCK_RESERVATION_RELEASE,
                InventoryEventType.STOCK_RESERVATION_CONFIRM);
    }

    @Test
    @DisplayName("재고 상태 조회 - 스냅샷이 없으면 OUT_OF_STOCK")
    void stockStatusFallsBackToOutOfStock() {
        when(inventoryRepository.findOne(1L, null, "MAIN")).thenReturn(Optional.empty());

        assertThat(inventoryService.getStockStatus(1L, null, "MAIN"))
            .isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("재고 상태 조회 - 스냅샷 상태를 반환한다")
    void stockStatusFromSnapshot() {
        when(inventoryRepository.findOne(1L, null, "MAIN"))
            .thenReturn(Optional.of(snapshot(50, 0)));

        assertThat(inventoryService.getStockStatus(1L, null, "MAIN"))
            .isEqualTo(StockStatus.IN_STOCK);
    }

    private InventoryChangeCommand command(int quantity) {
        return InventoryChangeCommand.builder()
            .productId(1L)
            .locationCode("MAIN")
            .quantity(quantity)
            .reason("테스트")
            .build();
    }

    private InventorySnapshot snapshot(int available, int reserved) {
        return InventorySnapshot.reconstitute(1L, 1L, null, "MAIN", available, reserved, 10,
            LocalDateTime.now(), 1L);
    }

}
