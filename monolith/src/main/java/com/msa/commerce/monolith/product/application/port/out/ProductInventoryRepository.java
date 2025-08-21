package com.msa.commerce.monolith.product.application.port.out;

import com.msa.commerce.monolith.product.domain.ProductInventory;

import java.util.Optional;

public interface ProductInventoryRepository {
    
    ProductInventory save(ProductInventory inventory);
    
    Optional<ProductInventory> findByProductId(Long productId);
    
    Optional<ProductInventory> findByProductVariantId(Long productVariantId);
    
    void deleteByProductId(Long productId);
    
    java.util.List<ProductInventory> findLowStockProducts();
    
    java.util.List<ProductInventory> findOutOfStockProducts();
}