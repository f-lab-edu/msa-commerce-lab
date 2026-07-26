package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.product.application.port.out.ProductVariantRepository;
import com.msa.commerce.monolith.product.domain.ProductVariant;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductVariantRepositoryAdapter implements ProductVariantRepository {

    private final ProductVariantJpaRepository productVariantJpaRepository;

    private final ProductVariantMapper productVariantMapper;

    @Override
    public ProductVariant save(ProductVariant variant) {
        ProductVariantJpaEntity saved = productVariantJpaRepository.save(productVariantMapper.toEntity(variant));
        return productVariantMapper.toDomain(saved);
    }

    @Override
    public Optional<ProductVariant> findById(Long id) {
        return productVariantJpaRepository.findById(id).map(productVariantMapper::toDomain);
    }

    @Override
    public Optional<ProductVariant> findByVariantSku(String variantSku) {
        return productVariantJpaRepository.findByVariantSku(variantSku).map(productVariantMapper::toDomain);
    }

    @Override
    public List<ProductVariant> findByProductId(Long productId) {
        return productVariantJpaRepository.findByProductId(productId).stream()
            .map(productVariantMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByVariantSku(String variantSku) {
        return productVariantJpaRepository.existsByVariantSku(variantSku);
    }

}
