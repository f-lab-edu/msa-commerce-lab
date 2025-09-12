package com.msa.commerce.monolith.product.domain.event;

import java.util.EnumSet;
import java.util.Set;

import com.msa.commerce.monolith.product.domain.Product;

public record ProductEvent(
    Product product,
    Long productId,
    EventType eventType,
    Set<EventAction> actions
) {

    public enum EventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED
    }

    public enum EventAction {
        EVICT_SINGLE_CACHE,     // 단일 상품 캐시 무효화
        EVICT_ALL_CACHE,        // 전체 상품 목록 캐시 무효화
        PUBLISH_EXTERNAL_EVENT,  // 외부 시스템에 이벤트 발행
        ADDITIONAL_CLEANUP      // 추가적인 캐시 정리
    }

    // 상품 삭제 이벤트 - 캐시 무효화 + 외부 이벤트 발행
    public static ProductEvent productDeleted(Product product) {
        return new ProductEvent(
            product,
            product.getId(),
            EventType.PRODUCT_DELETED,
            EnumSet.of(
                EventAction.EVICT_SINGLE_CACHE,
                EventAction.EVICT_ALL_CACHE,
                EventAction.PUBLISH_EXTERNAL_EVENT,
                EventAction.ADDITIONAL_CLEANUP
            )
        );
    }

    // 상품 생성 이벤트 - 목록 캐시 무효화
    public static ProductEvent productCreated(Product product) {
        return new ProductEvent(
            product,
            product.getId(),
            EventType.PRODUCT_CREATED,
            EnumSet.of(
                EventAction.EVICT_ALL_CACHE,
                EventAction.ADDITIONAL_CLEANUP
            )
        );
    }

    // 상품 수정 이벤트 - 캐시 무효화
    public static ProductEvent productUpdated(Product product) {
        return new ProductEvent(
            product,
            product.getId(),
            EventType.PRODUCT_UPDATED,
            EnumSet.of(
                EventAction.EVICT_SINGLE_CACHE,
                EventAction.EVICT_ALL_CACHE,
                EventAction.ADDITIONAL_CLEANUP
            )
        );
    }

}
