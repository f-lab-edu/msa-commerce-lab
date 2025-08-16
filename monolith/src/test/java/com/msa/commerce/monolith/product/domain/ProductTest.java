package com.msa.commerce.monolith.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Product 도메인 엔티티 테스트
 */
@DisplayName("Product 도메인 테스트")
class ProductTest {

    @Test
    @DisplayName("정상적인 상품 생성")
    void createProduct_Success() {
        // given
        String name = "테스트 상품";
        String description = "테스트 상품 설명";
        BigDecimal price = new BigDecimal("10000");
        Integer stockQuantity = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;
        String imageUrl = "http://example.com/image.jpg";

        // when
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .imageUrl(imageUrl)
                .build();

        // then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStockQuantity()).isEqualTo(stockQuantity);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getImageUrl()).isEqualTo(imageUrl);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("상품명이 null인 경우 예외 발생")
    void createProduct_NameIsNull_ThrowsException() {
        // given
        String name = null;
        BigDecimal price = new BigDecimal("10000");
        Integer stockQuantity = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품명은 필수입니다.");
    }

    @Test
    @DisplayName("상품명이 빈 문자열인 경우 예외 발생")
    void createProduct_NameIsEmpty_ThrowsException() {
        // given
        String name = "";
        BigDecimal price = new BigDecimal("10000");
        Integer stockQuantity = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품명은 필수입니다.");
    }

    @Test
    @DisplayName("가격이 0 이하인 경우 예외 발생")
    void createProduct_PriceIsZeroOrNegative_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = BigDecimal.ZERO;
        Integer stockQuantity = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("가격이 1000만원 초과인 경우 예외 발생")
    void createProduct_PriceExceedsLimit_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("10000001");
        Integer stockQuantity = 100;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 1000만원을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("재고 수량이 음수인 경우 예외 발생")
    void createProduct_StockQuantityIsNegative_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("10000");
        Integer stockQuantity = -1;
        ProductCategory category = ProductCategory.ELECTRONICS;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고 수량은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("카테고리가 null인 경우 예외 발생")
    void createProduct_CategoryIsNull_ThrowsException() {
        // given
        String name = "테스트 상품";
        BigDecimal price = new BigDecimal("10000");
        Integer stockQuantity = 100;
        ProductCategory category = null;

        // when & then
        assertThatThrownBy(() -> Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리는 필수입니다.");
    }

    @Test
    @DisplayName("재고 차감 성공")
    void decreaseStock_Success() {
        // given
        Product product = createValidProduct();
        int decreaseQuantity = 10;

        // when
        product.decreaseStock(decreaseQuantity);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고 부족시 차감 실패")
    void decreaseStock_InsufficientStock_ThrowsException() {
        // given
        Product product = createValidProduct();
        int decreaseQuantity = 200;

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(decreaseQuantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("재고 증가 성공")
    void increaseStock_Success() {
        // given
        Product product = createValidProduct();
        int increaseQuantity = 50;

        // when
        product.increaseStock(increaseQuantity);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(150);
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

    private Product createValidProduct() {
        return Product.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .imageUrl("http://example.com/image.jpg")
                .build();
    }
}