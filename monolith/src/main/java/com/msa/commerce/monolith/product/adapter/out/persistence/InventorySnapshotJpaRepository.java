package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa.commerce.monolith.product.domain.StockStatus;

public interface InventorySnapshotJpaRepository extends JpaRepository<InventorySnapshotJpaEntity, Long> {

    Optional<InventorySnapshotJpaEntity> findByProductIdAndLocationCode(Long productId, String locationCode);

    Optional<InventorySnapshotJpaEntity> findByProductIdAndVariantIdAndLocationCode(
        Long productId, Long variantId, String locationCode);

    List<InventorySnapshotJpaEntity> findByProductId(Long productId);

    List<InventorySnapshotJpaEntity> findByProductIdAndVariantId(Long productId, Long variantId);

    @Query("""
        SELECT is FROM InventorySnapshotJpaEntity is
        JOIN FETCH is.product p
        WHERE is.product.id = :productId
        """)
    List<InventorySnapshotJpaEntity> findByProductIdWithProduct(@Param("productId") Long productId);

    @Query("""
        SELECT is FROM InventorySnapshotJpaEntity is
        JOIN FETCH is.product p
        LEFT JOIN FETCH is.variant v
        WHERE is.availableQuantity <= is.lowStockThreshold
        AND is.availableQuantity > 0
        """)
    List<InventorySnapshotJpaEntity> findLowStockItems();

    @Query("""
        SELECT is FROM InventorySnapshotJpaEntity is
        JOIN FETCH is.product p
        LEFT JOIN FETCH is.variant v
        WHERE is.availableQuantity = 0
        """)
    List<InventorySnapshotJpaEntity> findOutOfStockItems();

    @Query("""
        SELECT is FROM InventorySnapshotJpaEntity is
        WHERE is.product.id IN :productIds
        AND is.locationCode = :locationCode
        """)
    List<InventorySnapshotJpaEntity> findByProductIdsAndLocationCode(
        @Param("productIds") List<Long> productIds,
        @Param("locationCode") String locationCode);

    @Query("""
        SELECT SUM(is.availableQuantity) FROM InventorySnapshotJpaEntity is
        WHERE is.product.id = :productId
        """)
    Integer getTotalAvailableQuantityByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT SUM(is.reservedQuantity) FROM InventorySnapshotJpaEntity is
        WHERE is.product.id = :productId
        """)
    Integer getTotalReservedQuantityByProductId(@Param("productId") Long productId);

    @Query("""
        SELECT COUNT(is) FROM InventorySnapshotJpaEntity is
        WHERE is.availableQuantity <= is.lowStockThreshold
        AND is.availableQuantity > 0
        """)
    long countLowStockItems();

    @Query("""
        SELECT COUNT(is) FROM InventorySnapshotJpaEntity is
        WHERE is.availableQuantity = 0
        """)
    long countOutOfStockItems();

    @Query("""
        SELECT is FROM InventorySnapshotJpaEntity is
        WHERE is.locationCode = :locationCode
        ORDER BY is.lastUpdatedAt DESC
        """)
    List<InventorySnapshotJpaEntity> findByLocationCodeOrderByLastUpdatedAtDesc(
        @Param("locationCode") String locationCode);

    @Query("""
        SELECT CASE WHEN COUNT(is) > 0 THEN true ELSE false END
        FROM InventorySnapshotJpaEntity is
        WHERE is.product.id = :productId
        AND (:variantId IS NULL AND is.variant IS NULL OR is.variant.id = :variantId)
        AND is.locationCode = :locationCode
        AND is.availableQuantity >= :requiredQuantity
        """)
    boolean canReserveStock(
        @Param("productId") Long productId,
        @Param("variantId") Long variantId,
        @Param("locationCode") String locationCode,
        @Param("requiredQuantity") int requiredQuantity);
}