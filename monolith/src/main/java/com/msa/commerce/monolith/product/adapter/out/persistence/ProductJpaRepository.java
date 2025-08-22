package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    java.util.Optional<ProductJpaEntity> findBySku(String sku);

    java.util.List<ProductJpaEntity> findByCategoryId(Long categoryId);

    java.util.List<ProductJpaEntity> findByIsFeaturedTrue();

}
