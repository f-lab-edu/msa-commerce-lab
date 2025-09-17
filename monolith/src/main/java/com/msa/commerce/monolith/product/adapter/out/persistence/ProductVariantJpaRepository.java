package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msa.commerce.monolith.product.domain.ProductVariantStatus;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, Long> {

    List<ProductVariantJpaEntity> findByProductId(Long productId);

    List<ProductVariantJpaEntity> findByProductIdAndStatus(Long productId, ProductVariantStatus status);

    Optional<ProductVariantJpaEntity> findByVariantSku(String variantSku);

    Optional<ProductVariantJpaEntity> findByProductIdAndIsDefaultTrue(Long productId);

    @Query("""
        SELECT pv FROM ProductVariantJpaEntity pv
        JOIN FETCH pv.product p
        WHERE pv.product.id = :productId
        AND pv.status = :status
        ORDER BY pv.isDefault DESC, pv.createdAt ASC
        """)
    List<ProductVariantJpaEntity> findByProductIdAndStatusWithProduct(
        @Param("productId") Long productId,
        @Param("status") ProductVariantStatus status);

    @Query("""
        SELECT pv FROM ProductVariantJpaEntity pv
        JOIN FETCH pv.product p
        WHERE pv.color = :color
        AND pv.status = 'ACTIVE'
        """)
    List<ProductVariantJpaEntity> findActiveVariantsByColor(@Param("color") String color);

    @Query("""
        SELECT pv FROM ProductVariantJpaEntity pv
        JOIN FETCH pv.product p
        WHERE pv.size = :size
        AND pv.status = 'ACTIVE'
        """)
    List<ProductVariantJpaEntity> findActiveVariantsBySize(@Param("size") String size);

    @Query("""
        SELECT COUNT(pv) FROM ProductVariantJpaEntity pv
        WHERE pv.product.id = :productId
        AND pv.status = 'ACTIVE'
        """)
    int countActiveVariantsByProductId(@Param("productId") Long productId);

    boolean existsByVariantSku(String variantSku);

    boolean existsByProductIdAndIsDefaultTrue(Long productId);

    @Query("""
        SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END
        FROM ProductVariantJpaEntity pv
        WHERE pv.variantSku = :variantSku
        AND pv.id <> :excludeId
        """)
    boolean existsByVariantSkuAndIdNot(@Param("variantSku") String variantSku, @Param("excludeId") Long excludeId);
}