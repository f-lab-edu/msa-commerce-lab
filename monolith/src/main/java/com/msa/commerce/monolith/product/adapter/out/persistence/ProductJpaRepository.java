package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long>, JpaSpecificationExecutor<ProductJpaEntity> {

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    Optional<ProductJpaEntity> findBySku(String sku);

    List<ProductJpaEntity> findByCategoryId(Long categoryId);

    List<ProductJpaEntity> findByIsFeaturedTrue();

}

