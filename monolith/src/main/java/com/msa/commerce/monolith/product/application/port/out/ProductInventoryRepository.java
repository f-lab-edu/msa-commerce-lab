package com.msa.commerce.monolith.product.application.port.out;

import java.util.Optional;

import com.msa.commerce.monolith.product.domain.ProductInventory;

public interface ProductInventoryRepository {

    ProductInventory save(ProductInventory inventory);

    Optional<ProductInventory> findByProductId(Long productId);

    Optional<ProductInventory> findByProductVariantId(Long productVariantId);

    void deleteByProductId(Long productId);

    java.util.List<ProductInventory> findLowStockProducts();

    java.util.List<ProductInventory> findOutOfStockProducts();

}
