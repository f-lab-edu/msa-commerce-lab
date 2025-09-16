package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.msa.commerce.monolith.product.fixture.ProductCommandFixture;

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

    @ParameterizedTest(name = "{0} - Product 생성자 검증 실패")
    @MethodSource("com.msa.commerce.monolith.product.fixture.ProductCommandFixture#invalidProductBuilderScenarios")
    @DisplayName("Product 생성자 검증 실패 시나리오")
    void createProduct_ValidationFailures_ThrowsException(String scenario, Supplier<Product> productSupplier, String expectedMessage) {
        // when & then
        assertThatThrownBy(productSupplier::get)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
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

    @ParameterizedTest(name = "{0} - 주문 수량 검증")
    @MethodSource("com.msa.commerce.monolith.product.fixture.ProductCommandFixture#orderQuantityValidationScenarios")
    @DisplayName("주문 수량 검증 시나리오")
    void isValidateOrderQuantity_VariousScenarios(String scenario, Product product, Integer requestedQuantity, boolean expected) {
        // when
        boolean validationResult = product.isValidateOrderQuantity(requestedQuantity);

        // then
        assertThat(validationResult).isEqualTo(expected);
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
