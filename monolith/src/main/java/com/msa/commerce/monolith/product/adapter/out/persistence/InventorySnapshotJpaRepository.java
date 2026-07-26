package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface InventorySnapshotJpaRepository extends JpaRepository<InventorySnapshotJpaEntity, Long> {

    String FIND_ONE_JPQL = """
        SELECT s FROM InventorySnapshotJpaEntity s
        WHERE s.productId = :productId
          AND ((:variantId IS NULL AND s.variantId IS NULL) OR s.variantId = :variantId)
          AND s.locationCode = :locationCode
        """;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(FIND_ONE_JPQL)
    Optional<InventorySnapshotJpaEntity> findForUpdate(@Param("productId") Long productId,
        @Param("variantId") Long variantId, @Param("locationCode") String locationCode);

    @Query(FIND_ONE_JPQL)
    Optional<InventorySnapshotJpaEntity> findOne(@Param("productId") Long productId,
        @Param("variantId") Long variantId, @Param("locationCode") String locationCode);

    List<InventorySnapshotJpaEntity> findByProductId(Long productId);

}
