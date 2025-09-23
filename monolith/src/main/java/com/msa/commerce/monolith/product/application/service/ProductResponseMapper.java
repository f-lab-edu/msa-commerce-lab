package com.msa.commerce.monolith.product.application.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.domain.Product;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductResponseMapper {

    /**
     * Product 도메인 객체를 ProductResponse로 변환
     * Inventory 필드들은 기본값 또는 ignore 처리
     */
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

    /**
     * Product 도메인 객체를 ProductSearchResponse로 변환
     * viewCount는 기본값 0L로 설정
     */
    @Mapping(target = "viewCount", constant = "0L")
    ProductSearchResponse toSearchResponse(Product product);

}
