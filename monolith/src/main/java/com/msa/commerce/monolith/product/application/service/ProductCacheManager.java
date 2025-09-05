package com.msa.commerce.monolith.product.application.service;

public interface ProductCacheManager {

    void evictProduct(Long productId);

    void evictProductLists();

}
