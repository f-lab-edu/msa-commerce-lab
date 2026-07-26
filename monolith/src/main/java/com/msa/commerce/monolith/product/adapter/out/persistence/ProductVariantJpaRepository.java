package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariantJpaEntity, Long> {

    Optional<ProductVariantJpaEntity> findByVariantSku(String variantSku);

    List<ProductVariantJpaEntity> findByProductId(Long productId);

    boolean existsByVariantSku(String variantSku);

}
