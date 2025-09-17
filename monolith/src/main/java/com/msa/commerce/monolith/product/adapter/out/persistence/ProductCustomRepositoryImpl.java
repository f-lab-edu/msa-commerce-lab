package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    private final QProductJpaEntity product = QProductJpaEntity.productJpaEntity;
    private final QProductCategoryJpaEntity category = QProductCategoryJpaEntity.productCategoryJpaEntity;
    private final QInventorySnapshotJpaEntity inventory = QInventorySnapshotJpaEntity.inventorySnapshotJpaEntity;
    private final QProductVariantJpaEntity variant = QProductVariantJpaEntity.productVariantJpaEntity;

    @Override
    public Page<ProductJpaEntity> searchProductsWithInventory(String keyword, Long categoryId,
            ProductStatus status, ProductType productType, BigDecimal minPrice, BigDecimal maxPrice,
            String brand, Boolean isFeatured, Pageable pageable) {

        BooleanBuilder whereClause = new BooleanBuilder();

        // 키워드 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            whereClause.and(
                product.name.containsIgnoreCase(keyword)
                .or(product.description.containsIgnoreCase(keyword))
                .or(product.searchTags.containsIgnoreCase(keyword))
                .or(product.brand.containsIgnoreCase(keyword))
            );
        }

        // 조건 필터
        if (categoryId != null) {
            whereClause.and(product.categoryId.eq(categoryId));
        }
        if (status != null) {
            whereClause.and(product.status.eq(status));
        }
        if (productType != null) {
            whereClause.and(product.productType.eq(productType));
        }
        if (minPrice != null) {
            whereClause.and(product.basePrice.goe(minPrice));
        }
        if (maxPrice != null) {
            whereClause.and(product.basePrice.loe(maxPrice));
        }
        if (brand != null && !brand.trim().isEmpty()) {
            whereClause.and(product.brand.eq(brand));
        }
        if (isFeatured != null) {
            whereClause.and(product.isFeatured.eq(isFeatured));
        }

        // 데이터 조회 with Fetch Join
        JPQLQuery<ProductJpaEntity> query = queryFactory
            .selectFrom(product)
            .leftJoin(product.category, category).fetchJoin()
            .leftJoin(product.inventorySnapshots, inventory).fetchJoin()
            .where(whereClause)
            .distinct();

        // 정렬
        List<OrderSpecifier<?>> orders = getOrderSpecifiers(pageable);
        for (OrderSpecifier<?> order : orders) {
            query.orderBy(order);
        }

        // 페이징
        List<ProductJpaEntity> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 개수
        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(whereClause)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Optional<ProductJpaEntity> findProductWithFullDetails(Long productId) {
        ProductJpaEntity result = queryFactory
            .selectFrom(product)
            .leftJoin(product.category, category).fetchJoin()
            .leftJoin(product.variants, variant).fetchJoin()
            .leftJoin(product.inventorySnapshots, inventory).fetchJoin()
            .where(product.id.eq(productId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ProductCategoryStats> getCategoryProductStats() {
        return queryFactory
            .select(Projections.bean(ProductCategoryStatsImpl.class,
                category.id.as("categoryId"),
                category.name.as("categoryName"),
                product.count().as("productCount"),
                new CaseBuilder()
                    .when(product.status.eq(ProductStatus.ACTIVE))
                    .then(1)
                    .otherwise(0)
                    .sum().as("activeProductCount")
            ))
            .from(category)
            .leftJoin(product).on(product.categoryId.eq(category.id))
            .where(category.isActive.eq(true))
            .groupBy(category.id, category.name)
            .orderBy(category.displayOrder.asc())
            .fetch()
            .stream()
            .map(ProductCategoryStats.class::cast)
            .toList();
    }

    @Override
    public List<ProductJpaEntity> findLowStockProducts(int threshold) {
        return queryFactory
            .selectFrom(product)
            .leftJoin(product.inventorySnapshots, inventory).fetchJoin()
            .where(
                product.status.eq(ProductStatus.ACTIVE)
                .and(inventory.availableQuantity.loe(threshold))
                .and(inventory.availableQuantity.gt(0))
            )
            .orderBy(inventory.availableQuantity.asc())
            .fetch();
    }

    @Override
    public List<ProductJpaEntity> findPopularProducts(int limit) {
        // 실제 구현에서는 주문/판매 테이블과 조인해야 하지만,
        // 현재는 조회수나 featured 상품으로 대체
        return queryFactory
            .selectFrom(product)
            .leftJoin(product.category, category).fetchJoin()
            .where(
                product.status.eq(ProductStatus.ACTIVE)
                .and(product.isFeatured.eq(true))
            )
            .orderBy(product.createdAt.desc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<BrandStats> getBrandProductStats() {
        return queryFactory
            .select(Projections.bean(BrandStatsImpl.class,
                product.brand.as("brand"),
                product.count().as("productCount"),
                product.basePrice.avg().as("averagePrice")
            ))
            .from(product)
            .where(
                product.status.eq(ProductStatus.ACTIVE)
                .and(product.brand.isNotNull())
                .and(product.brand.ne(""))
            )
            .groupBy(product.brand)
            .orderBy(product.count().desc())
            .fetch()
            .stream()
            .map(BrandStats.class::cast)
            .toList();
    }

    @Override
    public List<PriceRangeStats> getPriceRangeStats() {
        return queryFactory
            .select(Projections.bean(PriceRangeStatsImpl.class,
                Expressions.cases()
                    .when(product.basePrice.lt(BigDecimal.valueOf(10000))).then("0-1만원")
                    .when(product.basePrice.lt(BigDecimal.valueOf(50000))).then("1-5만원")
                    .when(product.basePrice.lt(BigDecimal.valueOf(100000))).then("5-10만원")
                    .when(product.basePrice.lt(BigDecimal.valueOf(500000))).then("10-50만원")
                    .otherwise("50만원 이상")
                    .as("priceRange"),
                product.count().as("productCount"),
                product.basePrice.min().as("minPrice"),
                product.basePrice.max().as("maxPrice")
            ))
            .from(product)
            .where(product.status.eq(ProductStatus.ACTIVE))
            .groupBy(Expressions.cases()
                .when(product.basePrice.lt(BigDecimal.valueOf(10000))).then("0-1만원")
                .when(product.basePrice.lt(BigDecimal.valueOf(50000))).then("1-5만원")
                .when(product.basePrice.lt(BigDecimal.valueOf(100000))).then("5-10만원")
                .when(product.basePrice.lt(BigDecimal.valueOf(500000))).then("10-50만원")
                .otherwise("50만원 이상")
            )
            .fetch()
            .stream()
            .map(PriceRangeStats.class::cast)
            .toList();
    }

    @Override
    public Page<ProductJpaEntity> fullTextSearch(String searchText, Pageable pageable) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String[] keywords = searchText.trim().split("\\s+");
        BooleanBuilder whereClause = new BooleanBuilder();

        for (String keyword : keywords) {
            String likeKeyword = "%" + keyword + "%";
            whereClause.and(
                product.name.containsIgnoreCase(keyword)
                .or(product.description.containsIgnoreCase(keyword))
                .or(product.searchTags.containsIgnoreCase(keyword))
                .or(product.brand.containsIgnoreCase(keyword))
            );
        }

        whereClause.and(product.status.eq(ProductStatus.ACTIVE));

        List<ProductJpaEntity> content = queryFactory
            .selectFrom(product)
            .leftJoin(product.category, category).fetchJoin()
            .where(whereClause)
            .orderBy(
                product.isFeatured.desc(),
                product.createdAt.desc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(whereClause)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<ProductJpaEntity> findRelatedProducts(Long productId, int limit) {
        ProductJpaEntity targetProduct = queryFactory
            .selectFrom(product)
            .where(product.id.eq(productId))
            .fetchOne();

        if (targetProduct == null) {
            return List.of();
        }

        return queryFactory
            .selectFrom(product)
            .leftJoin(product.category, category).fetchJoin()
            .where(
                product.id.ne(productId)
                .and(product.status.eq(ProductStatus.ACTIVE))
                .and(product.categoryId.eq(targetProduct.getCategoryId()))
                .and(product.basePrice.between(
                    targetProduct.getBasePrice().multiply(BigDecimal.valueOf(0.5)),
                    targetProduct.getBasePrice().multiply(BigDecimal.valueOf(1.5))
                ))
            )
            .orderBy(product.isFeatured.desc(), product.createdAt.desc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Long> findProductIdsForBatchProcessing(ProductStatus status, int batchSize, Long lastProcessedId) {
        BooleanBuilder whereClause = new BooleanBuilder();

        if (status != null) {
            whereClause.and(product.status.eq(status));
        }

        if (lastProcessedId != null) {
            whereClause.and(product.id.gt(lastProcessedId));
        }

        return queryFactory
            .select(product.id)
            .from(product)
            .where(whereClause)
            .orderBy(product.id.asc())
            .limit(batchSize)
            .fetch();
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isEmpty()) {
            // 기본 정렬: featured > 생성일자 desc
            orders.add(product.isFeatured.desc());
            orders.add(product.createdAt.desc());
        } else {
            pageable.getSort().forEach(order -> {
                switch (order.getProperty()) {
                    case "name" -> orders.add(order.isAscending() ? product.name.asc() : product.name.desc());
                    case "price" -> orders.add(order.isAscending() ? product.basePrice.asc() : product.basePrice.desc());
                    case "createdAt" -> orders.add(order.isAscending() ? product.createdAt.asc() : product.createdAt.desc());
                    case "brand" -> orders.add(order.isAscending() ? product.brand.asc() : product.brand.desc());
                    default -> {
                        // 알 수 없는 정렬 속성은 무시
                    }
                }
            });
        }

        return orders;
    }

    // 통계 구현 클래스들
    public static class ProductCategoryStatsImpl implements ProductCategoryStats {
        private Long categoryId;
        private String categoryName;
        private Long productCount;
        private Long activeProductCount;

        // getters and setters
        @Override
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        @Override
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        @Override
        public Long getProductCount() { return productCount; }
        public void setProductCount(Long productCount) { this.productCount = productCount; }

        @Override
        public Long getActiveProductCount() { return activeProductCount; }
        public void setActiveProductCount(Long activeProductCount) { this.activeProductCount = activeProductCount; }
    }

    public static class BrandStatsImpl implements BrandStats {
        private String brand;
        private Long productCount;
        private BigDecimal averagePrice;

        @Override
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        @Override
        public Long getProductCount() { return productCount; }
        public void setProductCount(Long productCount) { this.productCount = productCount; }

        @Override
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
    }

    public static class PriceRangeStatsImpl implements PriceRangeStats {
        private String priceRange;
        private Long productCount;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;

        @Override
        public String getPriceRange() { return priceRange; }
        public void setPriceRange(String priceRange) { this.priceRange = priceRange; }

        @Override
        public Long getProductCount() { return productCount; }
        public void setProductCount(Long productCount) { this.productCount = productCount; }

        @Override
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

        @Override
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    }
}