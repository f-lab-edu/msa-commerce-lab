package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryEventJpaRepository extends JpaRepository<InventoryEventJpaEntity, Long> {

    List<InventoryEventJpaEntity> findByProductIdAndOccurredAtAfterOrderByOccurredAtDesc(
        Long productId, LocalDateTime since);

}
