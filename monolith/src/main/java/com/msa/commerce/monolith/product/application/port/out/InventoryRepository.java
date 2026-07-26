package com.msa.commerce.monolith.product.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.msa.commerce.monolith.product.domain.InventoryEvent;
import com.msa.commerce.monolith.product.domain.InventorySnapshot;

public interface InventoryRepository {

    Optional<InventorySnapshot> findForUpdate(Long productId, Long variantId, String locationCode);

    Optional<InventorySnapshot> findOne(Long productId, Long variantId, String locationCode);

    InventorySnapshot save(InventorySnapshot snapshot);

    List<InventorySnapshot> findByProductId(Long productId);

    void appendEvent(InventoryEvent event);

    List<InventoryEvent> findEventsByProductId(Long productId, LocalDateTime since);

}
