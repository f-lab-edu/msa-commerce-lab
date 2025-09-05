package com.msa.commerce.monolith.product.adapter.out.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.service.ProductCacheManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductCacheManagerImpl implements ProductCacheManager {

    private static final String PRODUCT_CACHE = "products";

    private static final String PRODUCT_LIST_CACHE = "productLists";

    @Override
    @CacheEvict(value = PRODUCT_CACHE, key = "#productId")
    public void evictProduct(Long productId) {
        log.debug("Evicting cache for product: {}", productId);
    }

    @Override
    @CacheEvict(value = PRODUCT_LIST_CACHE, allEntries = true)
    public void evictProductLists() {
        log.debug("Evicting all product list caches");
    }

}
