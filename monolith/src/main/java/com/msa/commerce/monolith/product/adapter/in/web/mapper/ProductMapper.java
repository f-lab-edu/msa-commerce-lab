package com.msa.commerce.monolith.product.adapter.in.web.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.common.aop.ValidateResult;
import com.msa.commerce.monolith.product.adapter.in.web.ProductCreateRequest;
import com.msa.commerce.monolith.product.adapter.in.web.ProductSearchRequest;
import com.msa.commerce.monolith.product.adapter.in.web.ProductUpdateRequest;
import com.msa.commerce.monolith.product.adapter.in.web.ProductVerifyRequest;
import com.msa.commerce.monolith.product.application.port.in.command.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.command.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.command.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.command.ProductVerifyCommand;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "basePrice", source = "price")
    @Mapping(target = "salePrice", source = "comparePrice")
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "weightGrams", ignore = true)
    @Mapping(target = "requiresShipping", ignore = true)
    @Mapping(target = "isTaxable", ignore = true)
    @Mapping(target = "searchTags", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    @ValidateResult
    ProductCreateCommand toCommand(ProductCreateRequest request);

    @ValidateResult
    ProductSearchCommand toSearchCommand(ProductSearchRequest request);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "productType", ignore = true)
    @Mapping(target = "basePrice", source = "request.price")
    @Mapping(target = "salePrice", source = "request.comparePrice")
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "weightGrams", ignore = true)
    @Mapping(target = "requiresShipping", ignore = true)
    @Mapping(target = "isTaxable", ignore = true)
    @Mapping(target = "searchTags", ignore = true)
    @Mapping(target = "primaryImageUrl", ignore = true)
    @ValidateResult
    ProductUpdateCommand toUpdateCommand(Long productId, ProductUpdateRequest request);

    @ValidateResult
    ProductVerifyCommand toVerifyCommand(ProductVerifyRequest request);

    @AfterMapping
    default void applyCreateDefaults(@MappingTarget ProductCreateCommand.ProductCreateCommandBuilder target,
        ProductCreateRequest request) {
        if (request.getSku() == null) {
            target.sku(generateSkuFromName(request.getName()));
        } else {
            target.sku(request.getSku());
        }

        target.slug(generateSlugFromName(request.getName()));

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

        String[] words = name.trim().split("\\s+");
        String firstPart = words[0];

        String cleanName = firstPart.toUpperCase()
            .replaceAll("[^A-Z0-9가-힣]", "");

        if (cleanName.length() > 4) {
            cleanName = cleanName.substring(0, 4);
        }

        if (cleanName.isEmpty()) {
            cleanName = "PROD";
        }

        return cleanName + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String generateSlugFromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "product-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        return name.toLowerCase()
            .replaceAll("[^a-z0-9가-힣\\s]", "")
            .trim()
            .replaceAll("\\s+", "-");
    }

}

