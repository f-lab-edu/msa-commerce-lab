package com.msa.commerce.monolith.product.domain.validation;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductValidator 테스트")
class ProductValidatorTest {

    @Test
    @DisplayName("유효한 상품 생성 데이터 - 검증 성공")
    void validateValidProductCreation() {
        // when
        Notification notification = ProductValidator.validateProductCreation(
            1L, "TEST-SKU", "Test Product", new BigDecimal("10000"),
            "Description", "Short desc", "Brand", "Model",
            10, 5, 1, 100, 5, 20, "LOC001"
        );
        
        // then
        assertThat(notification.hasErrors()).isFalse();
    }
    
    @Test
    @DisplayName("필수 필드 누락 - 검증 실패")
    void validateProductCreationWithMissingRequiredFields() {
        // when
        Notification notification = ProductValidator.validateProductCreation(
            null, null, null, null,
            null, null, null, null,
            null, null, null, null, null, null, null
        );
        
        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorCount()).isEqualTo(4); // categoryId, sku, name, price
        assertThat(notification.getErrorMessages()).contains(
            "Category ID is required",
            "SKU is required",
            "Product name is required",
            "Price is required"
        );
    }
    
    @Test
    @DisplayName("잘못된 가격 범위 - 검증 실패")
    void validateProductCreationWithInvalidPrice() {
        // when
        Notification notification = ProductValidator.validateProductCreation(
            1L, "TEST-SKU", "Test Product", new BigDecimal("-100"),
            null, null, null, null,
            null, null, null, null, null, null, null
        );
        
        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorMessages()).contains("Price must be greater than 0");
    }
    
    @Test
    @DisplayName("유효한 상품 업데이트 데이터 - 검증 성공")
    void validateValidProductUpdate() {
        // when
        Notification notification = ProductValidator.validateProductUpdate(
            1L, "UPDATED-SKU", "Updated Product", new BigDecimal("20000"),
            null, null, null, null, null, null, null, null, null, null, null
        );
        
        // then
        assertThat(notification.hasErrors()).isFalse();
    }
    
    @Test
    @DisplayName("상품 ID 누락 업데이트 - 검증 실패")
    void validateProductUpdateWithoutProductId() {
        // when
        Notification notification = ProductValidator.validateProductUpdate(
            null, "UPDATED-SKU", "Updated Product", new BigDecimal("20000"),
            null, null, null, null, null, null, null, null, null, null, null
        );
        
        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorMessages()).contains("Product ID is required for update");
    }
    
    @Test
    @DisplayName("유효한 검색 파라미터 - 검증 성공")
    void validateValidProductSearch() {
        // when
        Notification notification = ProductValidator.validateProductSearch(
            0, 20, new BigDecimal("1000"), new BigDecimal("50000")
        );
        
        // then
        assertThat(notification.hasErrors()).isFalse();
    }
    
    @Test
    @DisplayName("잘못된 페이징 파라미터 - 검증 실패")
    void validateProductSearchWithInvalidPaging() {
        // when
        Notification notification = ProductValidator.validateProductSearch(
            -1, 150, null, null
        );
        
        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorMessages()).contains(
            "Page must be greater than or equal to 0",
            "Size must be between 1 and 100"
        );
    }
    
    @Test
    @DisplayName("잘못된 가격 범위 - 검증 실패")
    void validateProductSearchWithInvalidPriceRange() {
        // when
        Notification notification = ProductValidator.validateProductSearch(
            0, 20, new BigDecimal("50000"), new BigDecimal("1000")
        );
        
        // then
        assertThat(notification.hasErrors()).isTrue();
        assertThat(notification.getErrorMessages()).contains(
            "Minimum price cannot be greater than maximum price"
        );
    }
}
