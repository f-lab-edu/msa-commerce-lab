package com.msa.commerce.common.util;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.PathBuilder;

public final class QuerydslPageableSort {

    public static OrderSpecifier<?>[] toOrderSpecifiers(Path<?> root, Sort sort) {
        if (sort == null || sort.isUnsorted())
            return new OrderSpecifier[0];

        PathBuilder<?> entityPath = new PathBuilder<>(root.getType(), root.getMetadata());

        return sort.stream()
            .map(order -> new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                toComparableExpression(entityPath, order.getProperty())
            ))
            .toArray(OrderSpecifier[]::new);
    }

    private static ComparableExpressionBase<?> toComparableExpression(PathBuilder<?> entityPath, String property) {
        return entityPath.getComparable(property, Comparable.class);
    }

}
