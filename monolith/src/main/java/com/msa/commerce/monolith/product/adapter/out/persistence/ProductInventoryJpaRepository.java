package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductInventoryJpaRepository extends JpaRepository<ProductInventoryJpaEntity, Long> {

    java.util.Optional<ProductInventoryJpaEntity> findByProductId(Long productId);

    java.util.Optional<ProductInventoryJpaEntity> findByProductVariantId(Long productVariantId);

    void deleteByProductId(Long productId);

    @Query("SELECT pi FROM ProductInventoryJpaEntity pi WHERE pi.isTrackingEnabled = true AND pi.availableQuantity <= pi.lowStockThreshold")
    java.util.List<ProductInventoryJpaEntity> findLowStockProducts();

    @Query("SELECT pi FROM ProductInventoryJpaEntity pi WHERE pi.availableQuantity = 0")
    java.util.List<ProductInventoryJpaEntity> findOutOfStockProducts();

}
