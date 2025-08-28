package com.msa.commerce.monolith.product.application.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.domain.Product;

@Mapper(componentModel = "spring")
public interface ProductSearchMapper {

    @Mapping(target = "viewCount", ignore = true)
    ProductSearchResponse toSearchResponse(Product product);

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

}

