package com.msa.commerce.monolith.product.adapter.in.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;

@Mapper(componentModel = "spring")
public interface ProductSearchWebMapper {

    @Mapping(target = "page", defaultValue = "0")
    @Mapping(target = "size", defaultValue = "20")
    @Mapping(target = "sortBy", defaultValue = "createdAt")
    @Mapping(target = "sortDirection", defaultValue = "desc")
    ProductSearchCommand toCommand(ProductSearchRequest request);
}