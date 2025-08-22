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
        String name = "테스트 상품";
        String description = "테스트 상품 설명";
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when
        Product product = Product.builder()
            .name(name)
            .description(description)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build();

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getSku()).isEqualTo(sku);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.getVisibility()).isEqualTo("PUBLIC");
        assertThat(product.getIsFeatured()).isFalse();
    }

    @Test
    @DisplayName("상품명이 null인 경우 예외 발생")
    void createProduct_NameIsNull_ThrowsException() {
        // given
        String name = null;
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name is required.");
    }

    @Test
    @DisplayName("상품명이 빈 문자열인 경우 예외 발생")
    void createProduct_NameIsEmpty_ThrowsException() {
        // given
        String name = "";
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name is required.");
    }

    @Test
    @DisplayName("SKU가 null인 경우 예외 발생")
    void createProduct_SkuIsNull_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = null;

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SKU is required.");
    }

    @Test
    @DisplayName("가격이 0 이하인 경우 예외 발생")
    void createProduct_PriceIsZeroOrNegative_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = BigDecimal.ZERO;
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price must be greater than 0.");
    }

    @Test
    @DisplayName("가격이 99,999,999.99 초과인 경우 예외 발생")
    void createProduct_PriceExceedsLimit_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("100000000.00");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price cannot exceed 99,999,999.99.");
    }

    @Test
    @DisplayName("카테고리 ID가 null인 경우 예외 발생")
    void createProduct_CategoryIdIsNull_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = null;
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category ID is required.");
    }

    @Test
    @DisplayName("상품명이 255자 초과인 경우 예외 발생")
    void createProduct_NameExceedsMaxLength_ThrowsException() {
        // given
        String name = "a".repeat(256); // 256자
        BigDecimal price = new BigDecimal("10000");
        Long categoryId = ProductCategory.ELECTRONICS.getId();
        String sku = "TEST1234";

        // when & then
        assertThatThrownBy(() -> Product.builder()
            .name(name)
            .price(price)
            .categoryId(categoryId)
            .sku(sku)
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
        BigDecimal newPrice = new BigDecimal("20000");

        // when
        product.updateProductInfo(newName, newDescription, newPrice);

        // then
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo(newDescription);
        assertThat(product.getPrice()).isEqualTo(newPrice);
    }

    private Product createValidProduct() {
        return Product.builder()
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .price(new BigDecimal("10000"))
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST1234")
            .build();
    }

}
