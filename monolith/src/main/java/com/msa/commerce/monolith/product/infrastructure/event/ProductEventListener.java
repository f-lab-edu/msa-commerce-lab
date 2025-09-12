package com.msa.commerce.monolith.product.infrastructure.event;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.msa.commerce.monolith.product.application.service.ProductEventPublisher;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private static final String PRODUCT_CACHE = "product";

    private static final String PRODUCTS_CACHE = "products";

    private final CacheManager cacheManager;

    private final ProductEventPublisher productEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductEvent(ProductEvent event) {
        for (ProductEvent.EventAction action : event.actions()) {
            switch (action) {
                case EVICT_SINGLE_CACHE:
                    evictSingleProduct(event.productId());
                    break;
                case EVICT_ALL_CACHE:
                    evictAllProducts();
                    break;
                case PUBLISH_EXTERNAL_EVENT:
                    publishExternalEvent(event);
                    break;
                case ADDITIONAL_CLEANUP:
                    handleAdditionalCacheCleanup(event.productId());
                    break;
            }
        }
    }

    private void publishExternalEvent(ProductEvent event) {
        switch (event.eventType()) {
            case PRODUCT_DELETED:
                productEventPublisher.publishProductDeletedEvent(event.product());
                break;
            case PRODUCT_CREATED:
                // TODO: 상품 생성 이벤트 발행
                break;
            case PRODUCT_UPDATED:
                // TODO: 상품 수정 이벤트 발행
                break;
        }
    }

    private void evictSingleProduct(Long productId) {
        if (productId != null) {
            var cache = cacheManager.getCache(PRODUCT_CACHE);
            if (cache != null) {
                cache.evict(productId);
                log.debug("Evicted product cache for productId: {}", productId);
            }
        }
    }

    private void evictAllProducts() {
        var cache = cacheManager.getCache(PRODUCTS_CACHE);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared all products cache");
        }
    }

    private void handleAdditionalCacheCleanup(Long productId) {
        // TODO: 추가적인 캐시 정리 로직
        //  e.g. 카테고리별 상품 목록 캐시, 검색 결과 캐시 등
    }

}
