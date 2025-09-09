package com.msa.commerce.monolith.product.adapter.in.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.common.aop.ValidateResult;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;

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

}
