package com.msa.commerce.monolith.product.adapter.in.web;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductWebMapperTest {

    private ProductWebMapper productWebMapper;

    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        productWebMapper = new ProductWebMapper();
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
        ProductCreateCommand command = productWebMapper.toCommand(createRequest);

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
        ProductCreateCommand command = productWebMapper.toCommand(requestWithNulls);

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