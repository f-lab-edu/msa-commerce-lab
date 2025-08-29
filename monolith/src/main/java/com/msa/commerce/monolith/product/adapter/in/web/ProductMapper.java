package com.msa.commerce.monolith.product.adapter.in.web;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.common.aop.ValidateResult;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "visibility", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @ValidateResult
    ProductCreateCommand toCommand(ProductCreateRequest request);

    @ValidateResult
    ProductSearchCommand toSearchCommand(ProductSearchRequest request);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "visibility", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @ValidateResult
    ProductUpdateCommand toUpdateCommand(Long productId, ProductUpdateRequest request);

    @AfterMapping
    default void applyCreateDefaults(@MappingTarget ProductCreateCommand.ProductCreateCommandBuilder target,
        ProductCreateRequest request) {
        if (request.getSku() == null) {
            target.sku(generateSkuFromName(request.getName()));
        } else {
            target.sku(request.getSku());
        }

        if (request.getVisibility() == null) {
            target.visibility("PUBLIC");
        } else {
            target.visibility(request.getVisibility());
        }

        if (request.getIsFeatured() == null) {
            target.isFeatured(false);
        } else {
            target.isFeatured(request.getIsFeatured());
        }
    }

    private static String generateSkuFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "PROD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        // Take first word or first few characters
        String[] words = name.trim().split("\\s+");
        String firstPart = words[0]; // First word

        String cleanName = firstPart.toUpperCase()
            .replaceAll("[^A-Z0-9가-힣]", "");

        // Take first 4 characters or less
        if (cleanName.length() > 4) {
            cleanName = cleanName.substring(0, 4);
        }

        if (cleanName.isEmpty()) {
            cleanName = "PROD";
        }

        return cleanName + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
