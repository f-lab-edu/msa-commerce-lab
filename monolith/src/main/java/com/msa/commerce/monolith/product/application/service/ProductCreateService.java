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
import com.msa.commerce.monolith.product.domain.validation.Notification;
import com.msa.commerce.monolith.product.domain.validation.ValidationException;

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
        command.validate();
        validateDuplicateSku(command.getSku());
        return executeProductCreation(command);
    }

    public ProductResponse createProductWithNotification(ProductCreateCommand command) {
        Notification inputValidation = command.validateWithNotification();

        Notification businessValidation = validateBusinessRules(command);

        Notification combinedValidation = combineValidations(inputValidation, businessValidation);

        if (combinedValidation.hasErrors()) {
            throw new ValidationException(combinedValidation.getErrors());
        }

        return executeProductCreation(command);
    }

    private Notification validateBusinessRules(ProductCreateCommand command) {
        Notification notification = new Notification();

        if (command.getSku() != null && productRepository.existsBySku(command.getSku())) {
            notification.addError("sku", "Product SKU already exists: " + command.getSku());
        }


        return notification;
    }

    private Notification combineValidations(Notification... validations) {
        Notification combined = new Notification();

        for (Notification validation : validations) {
            if (validation.hasErrors()) {
                validation.getErrors().forEach(error ->
                    combined.addError(error.getField(), error.getMessage()));
            }
        }

        return combined;
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
