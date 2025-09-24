package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

public interface ProductCustomRepository {

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

    Optional<ProductJpaEntity> findProductWithFullDetails(Long productId);

    List<ProductCategoryStats> getCategoryProductStats();

    List<ProductJpaEntity> findLowStockProducts(int threshold);

    List<ProductJpaEntity> findPopularProducts(int limit);

    List<BrandStats> getBrandProductStats();

    List<PriceRangeStats> getPriceRangeStats();

    Page<ProductJpaEntity> fullTextSearch(String searchText, Pageable pageable);

    List<ProductJpaEntity> findRelatedProducts(Long productId, int limit);

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
