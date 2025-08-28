package com.msa.commerce.monolith.product.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;

    private final ProductResponseMapper productResponseMapper;

    @Override
    public ProductResponse createProduct(ProductCreateCommand command) {
        command.validate();
        validateDuplicateSku(command.getSku());
        return executeProductCreation(command);
    }

    private ProductResponse executeProductCreation(ProductCreateCommand command) {
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

        Product savedProduct = productRepository.save(product);
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

