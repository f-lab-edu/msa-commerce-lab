package com.msa.commerce.monolith.product.adapter.in.web;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;

@Component
public class ProductWebMapper {

    public ProductCreateCommand toCommand(ProductCreateRequest request) {
        if (request == null) {
            return null;
        }

        return ProductCreateCommand.builder()
            .categoryId(request.getCategoryId())
            .sku(request.getSku() != null ? request.getSku() : generateSku(request.getName()))
            .name(request.getName())
            .description(request.getDescription())
            .shortDescription(request.getShortDescription())
            .brand(request.getBrand())
            .model(request.getModel())
            .price(request.getPrice())
            .comparePrice(request.getComparePrice())
            .costPrice(request.getCostPrice())
            .weight(request.getWeight())
            .productAttributes(request.getProductAttributes())
            .visibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC")
            .taxClass(request.getTaxClass())
            .metaTitle(request.getMetaTitle())
            .metaDescription(request.getMetaDescription())
            .searchKeywords(request.getSearchKeywords())
            .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
            .initialStock(request.getInitialStock())
            .lowStockThreshold(request.getLowStockThreshold())
            .isTrackingEnabled(request.getIsTrackingEnabled())
            .isBackorderAllowed(request.getIsBackorderAllowed())
            .minOrderQuantity(request.getMinOrderQuantity())
            .maxOrderQuantity(request.getMaxOrderQuantity())
            .reorderPoint(request.getReorderPoint())
            .reorderQuantity(request.getReorderQuantity())
            .locationCode(request.getLocationCode())
            .build();
    }

    private String generateSku(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        String cleanName = name.toUpperCase()
            .replaceAll("[^A-Z0-9가-힣]", "")
            .substring(0, Math.min(name.length(), 4));

        if (cleanName.isEmpty()) {
            cleanName = "PROD";
        }

        return cleanName + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public ProductUpdateCommand toUpdateCommand(Long productId, ProductUpdateRequest request) {
        if (request == null) {
            return null;
        }

        return ProductUpdateCommand.builder()
            .productId(productId)
            .categoryId(request.getCategoryId())
            .sku(request.getSku())
            .name(request.getName())
            .description(request.getDescription())
            .shortDescription(request.getShortDescription())
            .brand(request.getBrand())
            .model(request.getModel())
            .price(request.getPrice())
            .comparePrice(request.getComparePrice())
            .costPrice(request.getCostPrice())
            .weight(request.getWeight())
            .productAttributes(request.getProductAttributes())
            .visibility(request.getVisibility())
            .taxClass(request.getTaxClass())
            .metaTitle(request.getMetaTitle())
            .metaDescription(request.getMetaDescription())
            .searchKeywords(request.getSearchKeywords())
            .isFeatured(request.getIsFeatured())
            .initialStock(request.getInitialStock())
            .lowStockThreshold(request.getLowStockThreshold())
            .isTrackingEnabled(request.getIsTrackingEnabled())
            .isBackorderAllowed(request.getIsBackorderAllowed())
            .minOrderQuantity(request.getMinOrderQuantity())
            .maxOrderQuantity(request.getMaxOrderQuantity())
            .reorderPoint(request.getReorderPoint())
            .reorderQuantity(request.getReorderQuantity())
            .locationCode(request.getLocationCode())
            .build();
    }

}
