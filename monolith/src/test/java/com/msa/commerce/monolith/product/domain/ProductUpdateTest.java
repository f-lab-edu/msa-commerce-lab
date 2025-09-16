package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product 업데이트 메소드 테스트")
class ProductUpdateTest {

    // Note: This test class tests the old Product.updatePartially method
    // which no longer exists in the current Product domain model.
    // These tests should be updated to use the new field structure.

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.reconstitute(
            1L,                                    // id
            "ORIGINAL-SKU",                       // sku
            "원본 상품명",                         // name
            "원본 짧은 설명",                      // shortDescription
            "원본 상품 설명",                      // description
            1L,                                   // categoryId
            "원본 브랜드",                         // brand
            ProductType.PHYSICAL,                 // productType
            ProductStatus.ACTIVE,                 // status
            new BigDecimal("10000"),              // basePrice
            new BigDecimal("12000"),              // salePrice
            "KRW",                               // currency
            1500,                                 // weightGrams
            true,                                 // requiresShipping
            true,                                 // isTaxable
            false,                                // isFeatured
            "original-product",                  // slug
            "검색 키워드",                         // searchTags
            null,                                 // primaryImageUrl
            1,                                    // minOrderQuantity
            100,                                  // maxOrderQuantity
            LocalDateTime.now().minusDays(1),     // createdAt
            LocalDateTime.now().minusDays(1),     // updatedAt
            1L                                    // version
        );
    }

    @Test
    @DisplayName("부분 업데이트 - 모든 필드 업데이트")
    void updatePartially_AllFields_Success() {
        // given
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // when
        product.updatePartially(
            "NEW-SKU",                            // sku
            "새로운 상품명",                       // name
            "새로운 짧은 설명",                    // shortDescription
            "새로운 상품 설명",                    // description
            2L,                                   // categoryId
            "새로운 브랜드",                       // brand
            ProductType.DIGITAL,                  // productType
            new BigDecimal("20000"),              // basePrice
            new BigDecimal("25000"),              // salePrice
            "USD",                               // currency
            2000,                                 // weightGrams
            false,                                // requiresShipping
            false,                                // isTaxable
            true,                                 // isFeatured
            "new-product",                       // slug
            "새로운 검색 키워드",                  // searchTags
            "http://example.com/image.jpg",      // primaryImageUrl
            2,                                    // minOrderQuantity
            50                                    // maxOrderQuantity
        );

        // then
        assertThat(product.getCategoryId()).isEqualTo(2L);
        assertThat(product.getSku()).isEqualTo("NEW-SKU");
        assertThat(product.getName()).isEqualTo("새로운 상품명");
        assertThat(product.getDescription()).isEqualTo("새로운 상품 설명");
        assertThat(product.getShortDescription()).isEqualTo("새로운 짧은 설명");
        assertThat(product.getBrand()).isEqualTo("새로운 브랜드");
        assertThat(product.getProductType()).isEqualTo(ProductType.DIGITAL);
        assertThat(product.getBasePrice()).isEqualTo(new BigDecimal("20000"));
        assertThat(product.getSalePrice()).isEqualTo(new BigDecimal("25000"));
        assertThat(product.getCurrency()).isEqualTo("USD");
        assertThat(product.getWeightGrams()).isEqualTo(2000);
        assertThat(product.getRequiresShipping()).isFalse();
        assertThat(product.getIsTaxable()).isFalse();
        assertThat(product.getSearchTags()).isEqualTo("새로운 검색 키워드");
        assertThat(product.getSlug()).isEqualTo("new-product");
        assertThat(product.getPrimaryImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(product.getIsFeatured()).isTrue();
        assertThat(product.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("부분 업데이트 - 일부 필드만 업데이트")
    void updatePartially_SomeFields_Success() {
        // given
        String originalSku = product.getSku();
        String originalDescription = product.getDescription();
        BigDecimal originalPrice = product.getBasePrice();

        // when - name과 brand만 업데이트
        product.updatePartially(
            null,             // sku - 변경 안함
            "새로운 상품명",   // name - 변경
            null,             // shortDescription - 변경 안함
            null,             // description - 변경 안함
            null,             // categoryId - 변경 안함
            "새로운 브랜드",   // brand - 변경
            null,             // productType - 변경 안함
            null,             // basePrice - 변경 안함
            null,             // salePrice - 변경 안함
            null,             // currency - 변경 안함
            null,             // weightGrams - 변경 안함
            null,             // requiresShipping - 변경 안함
            null,             // isTaxable - 변경 안함
            null,             // isFeatured - 변경 안함
            null,             // slug - 변경 안함
            null,             // searchTags - 변경 안함
            null,             // primaryImageUrl - 변경 안함
            null,             // minOrderQuantity - 변경 안함
            null              // maxOrderQuantity - 변경 안함
        );

        // then
        assertThat(product.getName()).isEqualTo("새로운 상품명");
        assertThat(product.getBrand()).isEqualTo("새로운 브랜드");

        // 변경되지 않은 필드들 확인
        assertThat(product.getSku()).isEqualTo(originalSku);
        assertThat(product.getDescription()).isEqualTo(originalDescription);
        assertThat(product.getBasePrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("부분 업데이트 - null 필드는 변경되지 않음")
    void updatePartially_NullFields_NoChange() {
        // given
        String originalName = product.getName();
        BigDecimal originalPrice = product.getBasePrice();

        // when - 모든 필드를 null로 업데이트 시도
        product.updatePartially(
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null
        );

        // then
        assertThat(product.getName()).isEqualTo(originalName);
        assertThat(product.getBasePrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("업데이트 가능 상태 확인 - ACTIVE 상품")
    void isUpdatable_ActiveProduct_ReturnsTrue() {
        // given
        Product activeProduct = Product.reconstitute(
            1L, "SKU", "이름", null, "설명",
            1L, null, ProductType.PHYSICAL, ProductStatus.ACTIVE,
            new BigDecimal("10000"), null, "KRW", null, true,
            true, false, "active-product", null, null,
            1, 100,
            LocalDateTime.now(), LocalDateTime.now(), 1L
        );

        // when & then
        assertThat(activeProduct.isUpdatable()).isTrue();
    }

    @Test
    @DisplayName("업데이트 가능 상태 확인 - ARCHIVED 상품")
    void isUpdatable_ArchivedProduct_ReturnsFalse() {
        // given
        Product archivedProduct = Product.reconstitute(
            1L, "SKU", "이름", null, "설명",
            1L, null, ProductType.PHYSICAL, ProductStatus.ARCHIVED,
            new BigDecimal("10000"), null, "KRW", null, true,
            true, false, "archived-product", null, null,
            1, 100,
            LocalDateTime.now(), LocalDateTime.now(), 1L
        );

        // when & then
        assertThat(archivedProduct.isUpdatable()).isFalse();
    }

    @Test
    @DisplayName("사용자별 업데이트 권한 확인")
    void canBeUpdatedBy_User_ReturnsCorrectly() {
        // given
        String userId = "user123";

        // when & then
        assertThat(product.canBeUpdatedBy(userId)).isTrue(); // ACTIVE 상품이므로 업데이트 가능
    }

    @Test
    @DisplayName("기존 업데이트 메소드와의 호환성 확인")
    void updateProductInfo_StillWorks() {
        // given
        String newName = "기존 메소드로 변경된 이름";
        String newDescription = "기존 메소드로 변경된 설명";
        BigDecimal newPrice = new BigDecimal("50000");

        // when
        product.updateProductInfo(newName, newDescription, newPrice);

        // then
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo(newDescription);
        assertThat(product.getBasePrice()).isEqualTo(newPrice);
    }

}
