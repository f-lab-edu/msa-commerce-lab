package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.product.application.port.out.ProductCategoryRepository;
import com.msa.commerce.monolith.product.domain.ProductCategory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryAdapter implements ProductCategoryRepository {

    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return productCategoryJpaRepository.findById(id).map(productCategoryMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findAllActive() {
        return productCategoryJpaRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
            .map(productCategoryMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsActiveById(Long id) {
        return productCategoryJpaRepository.existsByIdAndIsActiveTrue(id);
    }

}
