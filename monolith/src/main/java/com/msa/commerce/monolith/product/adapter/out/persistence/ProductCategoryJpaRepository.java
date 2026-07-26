package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryJpaEntity, Long> {

    boolean existsByIdAndIsActiveTrue(Long id);

    List<ProductCategoryJpaEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

}
