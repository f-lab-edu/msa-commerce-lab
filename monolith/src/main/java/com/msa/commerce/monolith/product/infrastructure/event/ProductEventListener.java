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
        // 캐시 무효화 처리
        invalidateCache(event);

        // 외부 이벤트 발행
        publishExternalEvent(event);
    }

    private void invalidateCache(ProductEvent event) {
        switch (event.eventType()) {
            case PRODUCT_CREATED:
                evictProductsCache();
                break;

            case PRODUCT_UPDATED:
            case PRODUCT_DELETED:
                evictProductCache(event.productId());
                evictProductsCache();
                break;
        }
    }

    private void publishExternalEvent(ProductEvent event) {
        switch (event.eventType()) {
            case PRODUCT_DELETED:
                productEventPublisher.publishProductDeletedEvent(event.product());
                break;

            case PRODUCT_CREATED:
                log.debug("Product created event for productId: {}", event.productId());
                break;

            case PRODUCT_UPDATED:
                log.debug("Product updated event for productId: {}", event.productId());
                break;
        }
    }

    private void evictProductCache(Long productId) {
        if (productId != null) {
            var cache = cacheManager.getCache(PRODUCT_CACHE);
            if (cache != null) {
                cache.evict(productId);
                log.debug("Evicted product cache for productId: {}", productId);
            }
        }
    }

    private void evictProductsCache() {
        var cache = cacheManager.getCache(PRODUCTS_CACHE);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared all products cache");
        }
    }

}
