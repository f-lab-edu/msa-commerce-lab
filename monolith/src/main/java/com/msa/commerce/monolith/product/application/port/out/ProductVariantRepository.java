package com.msa.commerce.monolith.product.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msa.commerce.monolith.product.domain.ProductVariant;

public interface ProductVariantRepository {

    ProductVariant save(ProductVariant variant);

    Optional<ProductVariant> findById(Long id);

    Optional<ProductVariant> findByVariantSku(String variantSku);

    List<ProductVariant> findByProductId(Long productId);

    boolean existsByVariantSku(String variantSku);

}
