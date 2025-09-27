package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product 소프트 삭제 단위 테스트")
class ProductSoftDeleteTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
            .sku("TEST-SKU-001")
            .name("Test Product")
            .shortDescription("Short description")
            .description("Detailed description")
            .categoryId(1L)
            .brand("Test Brand")
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("10000"))
            .salePrice(new BigDecimal("8000"))
            .currency("KRW")
            .weightGrams(500)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("test-product")
            .searchTags("test,product")
            .primaryImageUrl("http://example.com/image.jpg")
            .build();
    }

    @Test
    @DisplayName("softDelete 호출 시 상태가 ARCHIVED로 변경되고 deletedAt이 설정된다")
    void softDelete_Success() {
        // given
        assertThat(product.getStatus()).isNotEqualTo(ProductStatus.ARCHIVED);
        assertThat(product.getDeletedAt()).isNull();
        assertThat(product.isDeleted()).isFalse();

        LocalDateTime beforeDelete = LocalDateTime.now();

        // when
        product.softDelete();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(product.getDeletedAt()).isAfterOrEqualTo(beforeDelete);
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 상품도 다시 softDelete를 호출할 수 있다")
    void softDelete_AlreadyDeleted() {
        // given
        product.softDelete();
        LocalDateTime firstDeletedAt = product.getDeletedAt();
        assertThat(product.isDeleted()).isTrue();

        // when
        product.softDelete(); // 다시 삭제

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(product.getDeletedAt()).isNotNull();
        assertThat(product.getDeletedAt()).isAfterOrEqualTo(firstDeletedAt);
        assertThat(product.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute로 생성한 삭제된 상품도 isDeleted가 true를 반환한다")
    void reconstitute_WithDeletedAt() {
        // given
        LocalDateTime deletedAt = LocalDateTime.now().minusHours(1);

        // when
        Product deletedProduct = Product.reconstitute(
            1L,
            "TEST-SKU-001",
            "Test Product",
            "Short description",
            "Detailed description",
            1L,
            "Test Brand",
            ProductType.PHYSICAL,
            ProductStatus.ARCHIVED,
            new BigDecimal("10000"),
            new BigDecimal("8000"),
            "KRW",
            500,
            true,
            true,
            false,
            "test-product",
            "test,product",
            "http://example.com/image.jpg",
            1,
            100,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1),
            deletedAt,
            1L
        );

        // then
        assertThat(deletedProduct.isDeleted()).isTrue();
        assertThat(deletedProduct.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(deletedProduct.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    @DisplayName("reconstitute로 생성한 삭제되지 않은 상품은 isDeleted가 false를 반환한다")
    void reconstitute_WithoutDeletedAt() {
        // when
        Product activeProduct = Product.reconstitute(
            1L,
            "TEST-SKU-001",
            "Test Product",
            "Short description",
            "Detailed description",
            1L,
            "Test Brand",
            ProductType.PHYSICAL,
            ProductStatus.ACTIVE,
            new BigDecimal("10000"),
            new BigDecimal("8000"),
            "KRW",
            500,
            true,
            true,
            false,
            "test-product",
            "test,product",
            "http://example.com/image.jpg",
            1,
            100,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1),
            null, // deletedAt이 null
            1L
        );

        // then
        assertThat(activeProduct.isDeleted()).isFalse();
        assertThat(activeProduct.getDeletedAt()).isNull();
        assertThat(activeProduct.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

}
