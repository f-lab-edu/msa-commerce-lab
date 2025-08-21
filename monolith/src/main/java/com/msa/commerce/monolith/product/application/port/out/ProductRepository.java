package com.msa.commerce.monolith.product.application.port.out;

import com.msa.commerce.monolith.product.domain.Product;

import java.util.Optional;

public interface ProductRepository {
    
    Product save(Product product);
    
    Optional<Product> findById(Long id);
    
    boolean existsByName(String name);
    
    boolean existsBySku(String sku);
    
    Optional<Product> findBySku(String sku);
    
    java.util.List<Product> findByCategoryId(Long categoryId);
    
    java.util.List<Product> findFeaturedProducts();
}