package com.msa.commerce.monolith.product.application.port.in;

import com.msa.commerce.monolith.product.domain.StockStatus;

public interface InventoryUseCase {

    void stockIn(InventoryChangeCommand command);

    void stockOut(InventoryChangeCommand command);

    void reserve(InventoryChangeCommand command);

    void releaseReservation(InventoryChangeCommand command);

    void confirmReservation(InventoryChangeCommand command);

    void adjust(InventoryChangeCommand command);

    StockStatus getStockStatus(Long productId, Long variantId, String locationCode);

}
