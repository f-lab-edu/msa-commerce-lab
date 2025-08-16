package com.msa.commerce.monolith.product.adapter.out.persistence;

import com.msa.commerce.monolith.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 JPA 리포지토리
 * 헥사고날 아키텍처의 아웃바운드 어댑터
 */
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    
    /**
     * 상품명으로 존재 여부 확인
     */
    boolean existsByName(String name);
}