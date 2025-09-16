package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product 도메인 테스트")
class ProductTest {

    @Test
    @DisplayName("정상적인 상품 생성")
    void createProduct_Success() {
        // given
        String sku = "TEST1234";
        String name = "테스트 상품";
        String shortDescription = "간단한 설명";
        String description = "테스트 상품 설명";
        Long categoryId = 1L;
        String brand = "TestBrand";
        ProductType productType = ProductType.PHYSICAL;
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";

        // when
        Product product = Product.builder()
            .sku(sku)
            .name(name)
            .shortDescription(shortDescription)
            .description(description)
            .categoryId(categoryId)
            .brand(brand)
            .productType(productType)
            .basePrice(basePrice)
            .slug(slug)
            .build();

        // then
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getShortDescription()).isEqualTo(shortDescription);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getBrand()).isEqualTo(brand);
        assertThat(product.getProductType()).isEqualTo(productType);
        assertThat(product.getBasePrice()).isEqualTo(basePrice);
        assertThat(product.getSlug()).isEqualTo(slug);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getCurrency()).isEqualTo("KRW");
        assertThat(product.getRequiresShipping()).isTrue();
        assertThat(product.getIsTaxable()).isTrue();
        assertThat(product.getIsFeatured()).isFalse();
        assertThat(product.getMinOrderQuantity()).isEqualTo(1);
        assertThat(product.getMaxOrderQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("상품명이 null인 경우 예외 발생")
    void createProduct_NameIsNull_ThrowsException() {
        // given
        String sku = "TEST1234";
        String name = null;
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name is required.");
    }

    @Test
    @DisplayName("상품명이 빈 문자열인 경우 예외 발생")
    void createProduct_NameIsEmpty_ThrowsException() {
        // given
        String sku = "TEST1234";
        String name = "";
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name is required.");
    }

    @Test
    @DisplayName("SKU가 null인 경우 예외 발생")
    void createProduct_SkuIsNull_ThrowsException() {
        // given
        String sku = null;
        String name = "테스트 상품";
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SKU is required.");
    }

    @Test
    @DisplayName("기본 가격이 0 이하인 경우 예외 발생")
    void createProduct_BasePriceIsZeroOrNegative_ThrowsException() {
        // given
        String sku = "TEST1234";
        String name = "테스트 상품";
        BigDecimal basePrice = BigDecimal.ZERO;
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Base price must be greater than 0.");
    }

    @Test
    @DisplayName("기본 가격이 최대 한도 초과인 경우 예외 발생")
    void createProduct_BasePriceExceedsLimit_ThrowsException() {
        // given
        String sku = "TEST1234";
        String name = "테스트 상품";
        BigDecimal basePrice = new BigDecimal("1000000000000.0000"); // 최대 한도 초과
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Base price cannot exceed 999,999,999,999.9999.");
    }

    @Test
    @DisplayName("상품명이 255자 초과인 경우 예외 발생")
    void createProduct_NameExceedsMaxLength_ThrowsException() {
        // given
        String sku = "TEST1234";
        String name = "a".repeat(256); // 256자
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot exceed 255 characters.");
    }

    @Test
    @DisplayName("상품 비활성화")
    void deactivate_Success() {
        // given
        Product product = createValidProduct();

        // when
        product.deactivate();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("상품 활성화")
    void activate_Success() {
        // given
        Product product = createValidProduct();
        product.deactivate();

        // when
        product.activate();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("상품 아카이브")
    void archive_Success() {
        // given
        Product product = createValidProduct();

        // when
        product.archive();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    @DisplayName("상품 정보 업데이트")
    void updateProductInfo_Success() {
        // given
        Product product = createValidProduct();
        String newName = "업데이트된 상품명";
        String newDescription = "업데이트된 설명";
        BigDecimal newBasePrice = new BigDecimal("20000");

        // when
        product.updateProductInfo(newName, newDescription, newBasePrice);

        // then
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo(newDescription);
        assertThat(product.getBasePrice()).isEqualTo(newBasePrice);
    }

    @Test
    @DisplayName("사용자 정의 최소/최대 주문 수량으로 상품 생성")
    void createProduct_WithCustomOrderQuantities_Success() {
        // given
        String sku = "TEST1234";
        String name = "테스트 상품";
        BigDecimal basePrice = new BigDecimal("10000");
        String slug = "test-product";
        Integer minOrderQuantity = 5;
        Integer maxOrderQuantity = 50;

        // when
        Product product = Product.builder()
            .sku(sku)
            .name(name)
            .basePrice(basePrice)
            .slug(slug)
            .minOrderQuantity(minOrderQuantity)
            .maxOrderQuantity(maxOrderQuantity)
            .build();

        // then
        assertThat(product.getMinOrderQuantity()).isEqualTo(minOrderQuantity);
        assertThat(product.getMaxOrderQuantity()).isEqualTo(maxOrderQuantity);
    }

    @Test
    @DisplayName("유효한 수량 검증 - 성공")
    void isValidateOrderQuantity_ValidQuantity_ReturnsTrue() {
        // given
        Product product = Product.builder()
            .sku("TEST1234")
            .name("테스트 상품")
            .basePrice(new BigDecimal("10000"))
            .slug("test-product")
            .minOrderQuantity(2)
            .maxOrderQuantity(10)
            .build();
        Integer requestedQuantity = 5;

        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isTrue();
    }

    @Test
    @DisplayName("최소 주문 수량 미만인 경우 검증 실패")
    void isValidateOrderQuantity_BelowMinimum_ReturnsFalse() {
        // given
        Product product = Product.builder()
            .sku("TEST1234")
            .name("테스트 상품")
            .basePrice(new BigDecimal("10000"))
            .slug("test-product")
            .minOrderQuantity(5)
            .maxOrderQuantity(20)
            .build();
        Integer requestedQuantity = 3;

        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isFalse();
    }

    @Test
    @DisplayName("최대 주문 수량 초과인 경우 검증 실패")
    void isValidateOrderQuantity_ExceedsMaximum_ReturnsFalse() {
        // given
        Product product = Product.builder()
            .sku("TEST1234")
            .name("테스트 상품")
            .basePrice(new BigDecimal("10000"))
            .slug("test-product")
            .minOrderQuantity(1)
            .maxOrderQuantity(10)
            .build();
        Integer requestedQuantity = 15;

        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isFalse();
    }

    @Test
    @DisplayName("주문 수량이 null인 경우 검증 실패")
    void isValidateOrderQuantity_NullQuantity_ReturnsFalse() {
        // given
        Product product = createValidProduct();
        Integer requestedQuantity = null;

        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isFalse();
    }

    @Test
    @DisplayName("주문 수량이 0 이하인 경우 검증 실패")
    void isValidateOrderQuantity_ZeroOrNegativeQuantity_ReturnsFalse() {
        // given
        Product product = createValidProduct();
        Integer requestedQuantity = 0;

        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isFalse();
    }

    private Product createValidProduct() {
        return Product.builder()
            .sku("TEST1234")
            .name("테스트 상품")
            .shortDescription("간단한 설명")
            .description("테스트 상품 설명")
            .categoryId(1L)
            .brand("TestBrand")
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("10000"))
            .slug("test-product")
            .build();
    }

}
