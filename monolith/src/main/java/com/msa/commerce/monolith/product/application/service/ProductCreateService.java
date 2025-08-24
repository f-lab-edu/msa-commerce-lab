package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductInventory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final ProductResponseMapper productResponseMapper;

    @Override
    public ProductResponse createProduct(ProductCreateCommand command) {
        // 1. 명령 유효성 검증
        command.validate();

        // 2. 비즈니스 룰 검증: 중복 SKU 체크
        validateDuplicateSku(command.getSku());

        // 3. 도메인 객체 생성 (DB 스키마에 맞게 수정) // Mapper로 뺌
        Product product = Product.builder()
            .categoryId(command.getCategoryId())
            .sku(command.getSku())
            .name(command.getName())
            .description(command.getDescription())
            .shortDescription(command.getShortDescription())
            .brand(command.getBrand())
            .model(command.getModel())
            .price(command.getPrice())
            .comparePrice(command.getComparePrice())
            .costPrice(command.getCostPrice())
            .weight(command.getWeight())
            .productAttributes(command.getProductAttributes())
            .visibility(command.getVisibility())
            .taxClass(command.getTaxClass())
            .metaTitle(command.getMetaTitle())
            .metaDescription(command.getMetaDescription())
            .searchKeywords(command.getSearchKeywords())
            .isFeatured(command.getIsFeatured())
            .build();

        // 4. 상품 저장
        Product savedProduct = productRepository.save(product);

        // 5. 재고 정보 생성 및 저장 (초기 재고가 있는 경우)
        if (command.getInitialStock() != null && command.getInitialStock() > 0) {
            ProductInventory inventory = ProductInventory.builder()
                .productId(savedProduct.getId())
                .availableQuantity(command.getInitialStock())
                .totalQuantity(command.getInitialStock())
                .lowStockThreshold(command.getLowStockThreshold())
                .isTrackingEnabled(command.getIsTrackingEnabled())
                .isBackorderAllowed(command.getIsBackorderAllowed())
                .build();

            productInventoryRepository.save(inventory);
        }

        // 6. 응답 객체 변환
        return productResponseMapper.toResponse(savedProduct);
    }

    private void validateDuplicateSku(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException(
                "Product SKU already exists: " + sku,
                ErrorCode.PRODUCT_SKU_DUPLICATE.getCode()
            );
        }
    }

}
