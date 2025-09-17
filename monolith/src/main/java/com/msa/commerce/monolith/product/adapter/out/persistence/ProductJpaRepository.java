package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long>, JpaSpecificationExecutor<ProductJpaEntity>, ProductCustomRepository {

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    Optional<ProductJpaEntity> findBySku(String sku);

    List<ProductJpaEntity> findByCategoryId(Long categoryId);

    List<ProductJpaEntity> findByIsFeaturedTrue();

    List<ProductJpaEntity> findFirst3ByOrderByIdAsc();

    List<ProductJpaEntity> findFirst5ByOrderByIdAsc();

    List<ProductJpaEntity> findFirst10ByOrderByIdAsc();

    List<ProductJpaEntity> findFirst20ByOrderByIdAsc();

}
