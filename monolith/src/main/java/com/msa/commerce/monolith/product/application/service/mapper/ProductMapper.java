package com.msa.commerce.monolith.product.application.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.application.port.in.command.ProductCreateCommand;
import com.msa.commerce.monolith.product.domain.Product;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductMapper {

    @Mapping(target = "availableQuantity", constant = "0")
    @Mapping(target = "reservedQuantity", constant = "0")
    @Mapping(target = "totalQuantity", constant = "0")
    @Mapping(target = "lowStockThreshold", constant = "0")
    @Mapping(target = "isTrackingEnabled", constant = "true")
    @Mapping(target = "isBackorderAllowed", constant = "false")
    @Mapping(target = "reorderPoint", constant = "10")
    @Mapping(target = "reorderQuantity", constant = "20")
    @Mapping(target = "locationCode", constant = "MAIN")
    ProductResponse toResponse(Product product);

    @Mapping(target = "viewCount", constant = "0L")
    ProductSearchResponse toSearchResponse(Product product);

    @Mapping(target = "viewCount", source = "viewCount")
    ProductSearchResponse toSearchResponseWithViewCount(Product product, Long viewCount);

    @Mapping(target = "content", source = "page.content")
    @Mapping(target = "page", source = "page.number")
    @Mapping(target = "size", source = "page.size")
    @Mapping(target = "totalElements", source = "page.totalElements")
    @Mapping(target = "totalPages", source = "page.totalPages")
    @Mapping(target = "first", source = "page.first")
    @Mapping(target = "last", source = "page.last")
    @Mapping(target = "hasNext", expression = "java(page.hasNext())")
    @Mapping(target = "hasPrevious", expression = "java(page.hasPrevious())")
    ProductPageResponse toPageResponse(Page<ProductSearchResponse> page);

    default Product toProduct(ProductCreateCommand command) {
        return Product.builder()
            .sku(command.getSku())
            .name(command.getName())
            .shortDescription(command.getShortDescription())
            .description(command.getDescription())
            .categoryId(command.getCategoryId())
            .brand(command.getBrand())
            .productType(command.getProductType())
            .basePrice(command.getBasePrice())
            .salePrice(command.getSalePrice())
            .currency(command.getCurrency())
            .weightGrams(command.getWeightGrams())
            .requiresShipping(command.getRequiresShipping())
            .isTaxable(command.getIsTaxable())
            .isFeatured(command.getIsFeatured())
            .slug(command.getSlug())
            .searchTags(command.getSearchTags())
            .primaryImageUrl(command.getPrimaryImageUrl())
            .minOrderQuantity(command.getMinOrderQuantity())
            .maxOrderQuantity(command.getMaxOrderQuantity())
            .build();
    }


}
