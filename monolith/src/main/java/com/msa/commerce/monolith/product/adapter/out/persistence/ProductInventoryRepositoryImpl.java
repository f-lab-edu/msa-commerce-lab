package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.domain.ProductInventory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductInventoryRepositoryImpl implements ProductInventoryRepository {

    private final ProductInventoryJpaRepository productInventoryJpaRepository;

    @Override
    public ProductInventory save(ProductInventory inventory) {
        ProductInventoryJpaEntity jpaEntity;

        if (inventory.getId() == null) {
            // 새로운 엔티티 생성
            jpaEntity = ProductInventoryJpaEntity.fromDomainEntityForCreation(inventory);
        } else {
            // 기존 엔티티 업데이트
            jpaEntity = ProductInventoryJpaEntity.fromDomainEntity(inventory);
        }

        ProductInventoryJpaEntity savedEntity = productInventoryJpaRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public Optional<ProductInventory> findByProductId(Long productId) {
        return productInventoryJpaRepository.findByProductId(productId)
            .map(ProductInventoryJpaEntity::toDomainEntity);
    }

    @Override
    public Optional<ProductInventory> findByProductVariantId(Long productVariantId) {
        return productInventoryJpaRepository.findByProductVariantId(productVariantId)
            .map(ProductInventoryJpaEntity::toDomainEntity);
    }

    @Override
    public void deleteByProductId(Long productId) {
        productInventoryJpaRepository.deleteByProductId(productId);
    }

    @Override
    public List<ProductInventory> findLowStockProducts() {
        return productInventoryJpaRepository.findLowStockProducts()
            .stream()
            .map(ProductInventoryJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductInventory> findOutOfStockProducts() {
        return productInventoryJpaRepository.findOutOfStockProducts()
            .stream()
            .map(ProductInventoryJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

}
