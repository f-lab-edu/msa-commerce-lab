package com.msa.commerce.monolith.product.application.port.out;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.domain.Product;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findFeaturedProducts();
    
    List<Product> findAllByIds(List<Long> ids);

    Page<Product> searchProducts(ProductSearchCommand command);

}
