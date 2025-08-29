package com.msa.commerce.monolith.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.common.exception.CommandValidationException;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AOP Command Validation Integration Test")
class AopValidationIntegrationTest {

    @Autowired
    private ProductCreateUseCase productCreateUseCase;

    @Autowired
    private ProductUpdateUseCase productUpdateUseCase;

    @Test
    @DisplayName("ProductCreateCommand 필수 필드 누락 시 CommandValidationException 발생")
    void productCreateCommandValidationShouldFail() {
        // Given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
            .categoryId(null) // @NotNull 위반
            .sku("") // @NotBlank 위반
            .name("") // @NotBlank 위반
            .price(BigDecimal.ZERO) // @DecimalMin 위반
            .build();

        // When & Then
        assertThatThrownBy(() -> productCreateUseCase.createProduct(invalidCommand))
            .isInstanceOf(CommandValidationException.class)
            .hasMessageContaining("Product creation validation failed")
            .hasMessageContaining("Command validation failed");
    }

    @Test
    @DisplayName("ProductUpdateCommand 검증 오류 시 CommandValidationException 발생")
    void productUpdateCommandValidationShouldFail() {
        // Given
        ProductUpdateCommand invalidCommand = ProductUpdateCommand.builder()
            .productId(null) // @NotNull 위반
            .price(BigDecimal.ZERO) // @DecimalMin 위반
            .build();

        // When & Then
        assertThatThrownBy(() -> productUpdateUseCase.updateProduct(invalidCommand))
            .isInstanceOf(CommandValidationException.class)
            .hasMessageContaining("Product update validation failed")
            .hasMessageContaining("Command validation failed");
    }

    @Test
    @DisplayName("유효한 ProductCreateCommand는 AOP 검증을 통과한다")
    void validProductCreateCommandShouldPassAopValidation() {
        // Given
        ProductCreateCommand validCommand = ProductCreateCommand.builder()
            .categoryId(1L)
            .sku("TEST-SKU-001")
            .name("Valid Test Product")
            .price(new BigDecimal("100.00"))
            .build();

        // When & Then
        // AOP 검증은 통과하지만 비즈니스 로직에서 실패할 수 있음 (예: 중복 SKU)
        // 이는 AOP 검증이 정상적으로 통과했음을 의미
        assertThatThrownBy(() -> productCreateUseCase.createProduct(validCommand))
            .isNotInstanceOf(CommandValidationException.class);
    }

    @Test
    @DisplayName("유효한 ProductUpdateCommand는 AOP 검증을 통과한다")
    void validProductUpdateCommandShouldPassAopValidation() {
        // Given
        ProductUpdateCommand validCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .name("Valid Updated Product")
            .price(new BigDecimal("150.00"))
            .build();

        // When & Then
        // AOP 검증은 통과하지만 비즈니스 로직에서 실패할 수 있음 (예: 존재하지 않는 Product ID)
        // 이는 AOP 검증이 정상적으로 통과했음을 의미
        assertThatThrownBy(() -> productUpdateUseCase.updateProduct(validCommand))
            .isNotInstanceOf(CommandValidationException.class);
    }
}