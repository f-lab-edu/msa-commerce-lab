package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private static final QProductJpaEntity product = QProductJpaEntity.productJpaEntity;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductJpaEntity> searchProducts(ProductSearchCommand command, Pageable pageable) {
        List<ProductJpaEntity> content = queryFactory.selectFrom(product)
            .where(filters(command))
            .orderBy(orderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, totalCount(command));
    }

    private long totalCount(ProductSearchCommand command) {
        JPAQuery<Long> countQuery = queryFactory.select(product.count())
            .from(product)
            .where(filters(command));
        Long total = countQuery.fetchOne();
        return total != null ? total : 0L;
    }

    private BooleanExpression[] filters(ProductSearchCommand command) {
        return new BooleanExpression[] {
            categoryIdEq(command.getCategoryId()),
            basePriceGoe(command.getMinPrice()),
            basePriceLoe(command.getMaxPrice()),
            statusEq(command.getStatus())
        };
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? product.categoryId.eq(categoryId) : null;
    }

    private BooleanExpression basePriceGoe(BigDecimal minPrice) {
        return minPrice != null ? product.basePrice.goe(minPrice) : null;
    }

    private BooleanExpression basePriceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? product.basePrice.loe(maxPrice) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }

    private OrderSpecifier<?>[] orderSpecifiers(Sort sort) {
        return sort.stream()
            .map(this::toOrderSpecifier)
            .toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        boolean asc = order.isAscending();
        return switch (order.getProperty()) {
            case "basePrice" -> asc ? product.basePrice.asc() : product.basePrice.desc();
            case "name" -> asc ? product.name.asc() : product.name.desc();
            case "updatedAt" -> asc ? product.updatedAt.asc() : product.updatedAt.desc();
            default -> asc ? product.createdAt.asc() : product.createdAt.desc();
        };
    }

}
