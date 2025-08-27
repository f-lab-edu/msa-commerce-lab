package com.msa.commerce.monolith.product.adapter.in.web.validation;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.monolith.product.adapter.in.web.ProductSearchRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("PriceRange 커스텀 밸리데이션 테스트")
class PriceRangeValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("minPrice가 maxPrice보다 작거나 같으면 유효하다")
    void validPriceRange() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMinPrice(new BigDecimal("50.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("minPrice와 maxPrice가 같으면 유효하다")
    void equalPricesAreValid() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMinPrice(new BigDecimal("100.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("minPrice가 maxPrice보다 크면 유효하지 않다")
    void invalidPriceRange() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMinPrice(new BigDecimal("100.00"));
        request.setMaxPrice(new BigDecimal("50.00"));

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Minimum price cannot be greater than maximum price");
    }

    @Test
    @DisplayName("minPrice만 설정된 경우 유효하다")
    void minPriceOnlyIsValid() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMinPrice(new BigDecimal("50.00"));

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("maxPrice만 설정된 경우 유효하다")
    void maxPriceOnlyIsValid() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setMaxPrice(new BigDecimal("100.00"));

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("가격이 설정되지 않은 경우 유효하다")
    void noPricesSetIsValid() {
        // Given
        ProductSearchRequest request = new ProductSearchRequest();

        // When
        Set<ConstraintViolation<ProductSearchRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

}
