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

        // root의 타입/메타데이터를 그대로 사용해서 PathBuilder 구성
        PathBuilder<?> entityPath = new PathBuilder<>(root.getType(), root.getMetadata());

        return sort.stream()
            .map(order -> new OrderSpecifier<>(
                order.isAscending() ? Order.ASC : Order.DESC,
                // 핵심: Comparable 보장
                toComparableExpression(entityPath, order.getProperty())
            ))
            .toArray(OrderSpecifier[]::new);
    }

    private static ComparableExpressionBase<?> toComparableExpression(PathBuilder<?> entityPath, String property) {
        // 필요하면 여기서 화이트리스트 검사/매핑 추가
        return entityPath.getComparable(property, Comparable.class);
    }

}
