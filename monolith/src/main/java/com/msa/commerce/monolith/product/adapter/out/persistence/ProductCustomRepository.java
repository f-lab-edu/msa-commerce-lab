package com.msa.commerce.monolith.product.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;

public interface ProductCustomRepository {

    Page<ProductJpaEntity> searchProducts(ProductSearchCommand command, Pageable pageable);

}
