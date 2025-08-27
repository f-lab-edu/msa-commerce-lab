package com.msa.commerce.monolith.product.adapter.in.web;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@DisplayName("ProductMapper 테스트")
class ProductMapperTest {

    private ProductMapper productMapper;

    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
        createRequest = new ProductCreateRequest();
        setField(createRequest, "sku", "TEST-SKU-001");
        setField(createRequest, "name", "Test Product");
        setField(createRequest, "description", "Test Description");
        setField(createRequest, "price", new BigDecimal("29.99"));
        setField(createRequest, "categoryId", 1L);
        setField(createRequest, "shortDescription", "Short desc");
        setField(createRequest, "brand", "Test Brand");
        setField(createRequest, "initialStock", 100);
        setField(createRequest, "minOrderQuantity", 1);
        setField(createRequest, "maxOrderQuantity", 50);
        setField(createRequest, "reorderPoint", 10);
        setField(createRequest, "reorderQuantity", 20);
        setField(createRequest, "locationCode", "MAIN");
    }

    @Test
    @DisplayName("ProductCreateRequest를 ProductCreateCommand로 변환할 수 있다")
    void toCommand_ShouldMapRequestToCommand() {
        // when
        ProductCreateCommand command = productMapper.toCommand(createRequest);

        // then
        assertThat(command).isNotNull();
        assertThat(command.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(command.getName()).isEqualTo("Test Product");
        assertThat(command.getDescription()).isEqualTo("Test Description");
        assertThat(command.getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(command.getCategoryId()).isEqualTo(1L);
        assertThat(command.getShortDescription()).isEqualTo("Short desc");
        assertThat(command.getBrand()).isEqualTo("Test Brand");
        assertThat(command.getInitialStock()).isEqualTo(100);
        assertThat(command.getVisibility()).isEqualTo("PUBLIC");
        assertThat(command.getIsFeatured()).isFalse();

        // 확장된 재고 필드 검증
        assertThat(command.getMinOrderQuantity()).isEqualTo(1);
        assertThat(command.getMaxOrderQuantity()).isEqualTo(50);
        assertThat(command.getReorderPoint()).isEqualTo(10);
        assertThat(command.getReorderQuantity()).isEqualTo(20);
        assertThat(command.getLocationCode()).isEqualTo("MAIN");
    }

    @Test
    @DisplayName("Request에서 null 값이 있어도 Command로 변환할 수 있다")
    void toCommand_ShouldHandleNullValues() {
        // given
        ProductCreateRequest requestWithNulls = new ProductCreateRequest();
        setField(requestWithNulls, "sku", "TEST-NULL-001");
        setField(requestWithNulls, "name", "Test Product");
        setField(requestWithNulls, "price", new BigDecimal("19.99"));
        setField(requestWithNulls, "categoryId", 2L);
        setField(requestWithNulls, "initialStock", 50);

        // when
        ProductCreateCommand command = productMapper.toCommand(requestWithNulls);

        // then
        assertThat(command).isNotNull();
        assertThat(command.getSku()).isEqualTo("TEST-NULL-001");
        assertThat(command.getName()).isEqualTo("Test Product");
        assertThat(command.getDescription()).isNull();
        assertThat(command.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(command.getCategoryId()).isEqualTo(2L);
        assertThat(command.getInitialStock()).isEqualTo(50);

        // 확장된 재고 필드는 null이어야 함 (기본값은 서비스에서 처리)
        assertThat(command.getMinOrderQuantity()).isNull();
        assertThat(command.getMaxOrderQuantity()).isNull();
        assertThat(command.getReorderPoint()).isNull();
        assertThat(command.getReorderQuantity()).isNull();
        assertThat(command.getLocationCode()).isNull();
    }

    @Test
    @DisplayName("ProductSearchRequest를 ProductSearchCommand로 변환할 수 있다")
    void toSearchCommand_ShouldMapRequestToCommand() {
        // given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCategoryId(1L);
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("100.00"));
        request.setStatus(ProductStatus.ACTIVE);
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("name");
        request.setSortDirection("asc");

        // when
        ProductSearchCommand command = productMapper.toSearchCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.getCategoryId()).isEqualTo(1L);
        assertThat(command.getMinPrice()).isEqualTo(new BigDecimal("10.00"));
        assertThat(command.getMaxPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(command.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(command.getPage()).isEqualTo(0);
        assertThat(command.getSize()).isEqualTo(20);
        assertThat(command.getSortBy()).isEqualTo("name");
        assertThat(command.getSortDirection()).isEqualTo("asc");
    }

    @Test
    @DisplayName("SKU가 null이면 자동 생성된다")
    void toCommand_ShouldGenerateSkuWhenNull() {
        // given
        ProductCreateRequest request = new ProductCreateRequest();
        setField(request, "sku", null);
        setField(request, "name", "테스트 상품");
        setField(request, "price", new BigDecimal("29.99"));
        setField(request, "categoryId", 1L);

        // when
        ProductCreateCommand command = productMapper.toCommand(request);

        // then
        assertThat(command.getSku()).isNotNull();
        assertThat(command.getSku()).matches("테스트-[A-F0-9]{8}");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

}
