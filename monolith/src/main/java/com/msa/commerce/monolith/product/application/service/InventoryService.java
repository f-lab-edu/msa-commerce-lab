package com.msa.commerce.monolith.product.application.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.InventoryChangeCommand;
import com.msa.commerce.monolith.product.application.port.in.InventoryUseCase;
import com.msa.commerce.monolith.product.application.port.out.InventoryRepository;
import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventoryEventType;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;
import com.msa.commerce.monolith.product.domain.StockStatus;

import lombok.RequiredArgsConstructor;

// 스냅샷을 비관적 락으로 잠근 뒤 도메인 연산을 적용하고,
// 스냅샷 갱신과 이벤트 적재를 하나의 트랜잭션으로 묶는다.
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService implements InventoryUseCase {

    private final InventoryRepository inventoryRepository;

    @Override
    public void stockIn(InventoryChangeCommand command) {
        InventorySnapshot snapshot = findOrCreateSnapshot(command);
        applyAndRecord(InventoryEventType.STOCK_IN, snapshot, command,
            s -> s.stockIn(command.getQuantity()));
    }

    @Override
    public void stockOut(InventoryChangeCommand command) {
        applyAndRecord(InventoryEventType.STOCK_OUT, requireSnapshot(command), command,
            s -> s.stockOut(command.getQuantity()));
    }

    @Override
    public void reserve(InventoryChangeCommand command) {
        applyAndRecord(InventoryEventType.STOCK_RESERVATION, requireSnapshot(command), command,
            s -> s.reserve(command.getQuantity()));
    }

    @Override
    public void releaseReservation(InventoryChangeCommand command) {
        applyAndRecord(InventoryEventType.STOCK_RESERVATION_RELEASE, requireSnapshot(command), command,
            s -> s.releaseReservation(command.getQuantity()));
    }

    @Override
    public void confirmReservation(InventoryChangeCommand command) {
        applyAndRecord(InventoryEventType.STOCK_RESERVATION_CONFIRM, requireSnapshot(command), command,
            s -> s.confirmReservation(command.getQuantity()));
    }

    @Override
    public void adjust(InventoryChangeCommand command) {
        applyAndRecord(InventoryEventType.STOCK_ADJUSTMENT, requireSnapshot(command), command,
            s -> s.adjustTo(command.getQuantity()));
    }

    @Override
    @Transactional(readOnly = true)
    public StockStatus getStockStatus(Long productId, Long variantId, String locationCode) {
        return inventoryRepository.findOne(productId, variantId, locationCode)
            .map(InventorySnapshot::stockStatus)
            .orElse(StockStatus.OUT_OF_STOCK);
    }

    private void applyAndRecord(InventoryEventType eventType, InventorySnapshot snapshot,
        InventoryChangeCommand command, Consumer<InventorySnapshot> operation) {
        int quantityBefore = snapshot.getAvailableQuantity();
        operation.accept(snapshot);
        InventorySnapshot saved = inventoryRepository.save(snapshot);
        inventoryRepository.appendEvent(InventoryEvent.record(eventType, saved, quantityBefore, context(command)));
    }

    private InventorySnapshot findOrCreateSnapshot(InventoryChangeCommand command) {
        return inventoryRepository.findForUpdate(
                command.getProductId(), command.getVariantId(), command.getLocationCode())
            .orElseGet(() -> InventorySnapshot.create(
                command.getProductId(), command.getVariantId(), command.getLocationCode(), null));
    }

    private InventorySnapshot requireSnapshot(InventoryChangeCommand command) {
        return inventoryRepository.findForUpdate(
                command.getProductId(), command.getVariantId(), command.getLocationCode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory snapshot not found: productId=%d".formatted(command.getProductId())));
    }

    private InventoryEvent.ChangeContext context(InventoryChangeCommand command) {
        return InventoryEvent.ChangeContext.builder()
            .reason(command.getReason())
            .referenceType(command.getReferenceType())
            .referenceId(command.getReferenceId())
            .build();
    }

}
