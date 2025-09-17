package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

public interface ProductCustomRepository {

    /**
     * 복합 조건으로 상품 검색 (N+1 문제 방지)
     */
    Page<ProductJpaEntity> searchProductsWithInventory(
        String keyword,
        Long categoryId,
        ProductStatus status,
        ProductType productType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String brand,
        Boolean isFeatured,
        Pageable pageable
    );

    /**
     * 상품과 관련된 모든 정보를 Fetch Join으로 조회
     */
    Optional<ProductJpaEntity> findProductWithFullDetails(Long productId);

    /**
     * 카테고리별 상품 통계
     */
    List<ProductCategoryStats> getCategoryProductStats();

    /**
     * 재고가 부족한 상품들 조회
     */
    List<ProductJpaEntity> findLowStockProducts(int threshold);

    /**
     * 인기 상품 조회 (판매량 기준)
     */
    List<ProductJpaEntity> findPopularProducts(int limit);

    /**
     * 브랜드별 상품 개수
     */
    List<BrandStats> getBrandProductStats();

    /**
     * 가격 범위별 상품 분포
     */
    List<PriceRangeStats> getPriceRangeStats();

    /**
     * 상품 전문 검색 (전체 텍스트 검색)
     */
    Page<ProductJpaEntity> fullTextSearch(String searchText, Pageable pageable);

    /**
     * 관련 상품 추천 (같은 카테고리, 비슷한 가격대)
     */
    List<ProductJpaEntity> findRelatedProducts(Long productId, int limit);

    /**
     * 배치 처리를 위한 상품 ID 목록 조회
     */
    List<Long> findProductIdsForBatchProcessing(ProductStatus status, int batchSize, Long lastProcessedId);

    // 통계 클래스들
    interface ProductCategoryStats {
        Long getCategoryId();
        String getCategoryName();
        Long getProductCount();
        Long getActiveProductCount();
    }

    interface BrandStats {
        String getBrand();
        Long getProductCount();
        BigDecimal getAveragePrice();
    }

    interface PriceRangeStats {
        String getPriceRange();
        Long getProductCount();
        BigDecimal getMinPrice();
        BigDecimal getMaxPrice();
    }
}