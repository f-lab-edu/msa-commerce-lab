package com.msa.commerce.monolith.product.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msa.commerce.monolith.product.domain.ProductCategory;

public interface ProductCategoryRepository {

    Optional<ProductCategory> findById(Long id);

    List<ProductCategory> findAllActive();

    boolean existsActiveById(Long id);

}
