package com.msa.commerce.monolith.product.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        ProductJpaEntity jpaEntity;

        if (product.getId() == null) {
            // 새로운 엔티티 생성
            jpaEntity = ProductJpaEntity.fromDomainEntityForCreation(product);
        } else {
            // 기존 엔티티 업데이트
            jpaEntity = ProductJpaEntity.fromDomainEntity(product);
        }

        ProductJpaEntity savedEntity = productJpaRepository.save(jpaEntity);
        return savedEntity.toDomainEntity();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
            .map(ProductJpaEntity::toDomainEntity);
    }

    @Override
    public boolean existsByName(String name) {
        return productJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsBySku(String sku) {
        return productJpaRepository.existsBySku(sku);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findBySku(sku)
            .map(ProductJpaEntity::toDomainEntity);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productJpaRepository.findByCategoryId(categoryId)
            .stream()
            .map(ProductJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Product> findFeaturedProducts() {
        return productJpaRepository.findByIsFeaturedTrue()
            .stream()
            .map(ProductJpaEntity::toDomainEntity)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Product> searchProducts(ProductSearchCommand command) {
        Specification<ProductJpaEntity> spec = ProductSpecification.withFilters(command);
        Pageable pageable = createPageable(command);

        Page<ProductJpaEntity> jpaEntityPage = productJpaRepository.findAll(spec, pageable);
        
        return jpaEntityPage.map(ProductJpaEntity::toDomainEntity);
    }

    private Pageable createPageable(ProductSearchCommand command) {
        Sort sort = createSort(command.getSortBy(), command.getSortDirection());
        return PageRequest.of(
            command.getPage() != null ? command.getPage() : 0, 
            command.getSize() != null ? command.getSize() : 20, 
            sort
        );
    }

    private Sort createSort(String sortBy, String sortDirection) {
        String actualSortBy = mapSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return Sort.by(direction, actualSortBy);
    }

    private String mapSortField(String sortBy) {
        if (sortBy == null) {
            return "createdAt";
        }
        
        return switch (sortBy.toLowerCase()) {
            case "price" -> "price";
            case "name" -> "name";
            case "createdat", "created", "newest", "oldest" -> "createdAt";
            case "updatedat", "updated" -> "updatedAt";
            default -> "createdAt";
        };
    }

}
