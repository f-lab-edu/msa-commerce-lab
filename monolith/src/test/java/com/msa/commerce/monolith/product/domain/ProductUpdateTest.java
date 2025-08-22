package com.msa.commerce.monolith.product.domain;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product 업데이트 메소드 테스트")
class ProductUpdateTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.reconstitute(
            1L,                                    // id
            ProductCategory.ELECTRONICS.getId(),   // categoryId
            "ORIGINAL-SKU",                       // sku
            "원본 상품명",                         // name
            "원본 상품 설명",                      // description
            "원본 짧은 설명",                      // shortDescription
            "원본 브랜드",                         // brand
            "원본 모델",                          // model
            new BigDecimal("10000"),              // price
            new BigDecimal("12000"),              // comparePrice
            new BigDecimal("8000"),               // costPrice
            new BigDecimal("1.5"),                // weight
            "{}",                                 // productAttributes
            ProductStatus.ACTIVE,                 // status
            "PUBLIC",                             // visibility
            "STANDARD",                           // taxClass
            "원본 메타 제목",                      // metaTitle
            "원본 메타 설명",                      // metaDescription
            "검색 키워드",                         // searchKeywords
            false,                                // isFeatured
            LocalDateTime.now().minusDays(1),     // createdAt
            LocalDateTime.now().minusDays(1)      // updatedAt
        );
    }

    @Test
    @DisplayName("부분 업데이트 - 모든 필드 업데이트")
    void updatePartially_AllFields_Success() {
        // given
        LocalDateTime beforeUpdate = product.getUpdatedAt();

        // when
        product.updatePartially(
            ProductCategory.CLOTHING.getId(),     // categoryId
            "NEW-SKU",                            // sku
            "새로운 상품명",                       // name
            "새로운 상품 설명",                    // description
            "새로운 짧은 설명",                    // shortDescription
            "새로운 브랜드",                       // brand
            "새로운 모델",                        // model
            new BigDecimal("20000"),              // price
            new BigDecimal("25000"),              // comparePrice
            new BigDecimal("15000"),              // costPrice
            new BigDecimal("2.0"),                // weight
            "{\"new\": \"attributes\"}",          // productAttributes
            "PRIVATE",                            // visibility
            "LUXURY",                             // taxClass
            "새로운 메타 제목",                    // metaTitle
            "새로운 메타 설명",                    // metaDescription
            "새로운 검색 키워드",                  // searchKeywords
            true                                  // isFeatured
        );

        // then
        assertThat(product.getCategoryId()).isEqualTo(ProductCategory.CLOTHING.getId());
        assertThat(product.getSku()).isEqualTo("NEW-SKU");
        assertThat(product.getName()).isEqualTo("새로운 상품명");
        assertThat(product.getDescription()).isEqualTo("새로운 상품 설명");
        assertThat(product.getShortDescription()).isEqualTo("새로운 짧은 설명");
        assertThat(product.getBrand()).isEqualTo("새로운 브랜드");
        assertThat(product.getModel()).isEqualTo("새로운 모델");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("20000"));
        assertThat(product.getComparePrice()).isEqualTo(new BigDecimal("25000"));
        assertThat(product.getCostPrice()).isEqualTo(new BigDecimal("15000"));
        assertThat(product.getWeight()).isEqualTo(new BigDecimal("2.0"));
        assertThat(product.getProductAttributes()).isEqualTo("{\"new\": \"attributes\"}");
        assertThat(product.getVisibility()).isEqualTo("PRIVATE");
        assertThat(product.getTaxClass()).isEqualTo("LUXURY");
        assertThat(product.getMetaTitle()).isEqualTo("새로운 메타 제목");
        assertThat(product.getMetaDescription()).isEqualTo("새로운 메타 설명");
        assertThat(product.getSearchKeywords()).isEqualTo("새로운 검색 키워드");
        assertThat(product.getIsFeatured()).isTrue();
        assertThat(product.getUpdatedAt()).isAfter(beforeUpdate);
    }

    @Test
    @DisplayName("부분 업데이트 - 일부 필드만 업데이트")
    void updatePartially_SomeFields_Success() {
        // given
        String originalSku = product.getSku();
        String originalDescription = product.getDescription();
        BigDecimal originalPrice = product.getPrice();

        // when - name과 brand만 업데이트
        product.updatePartially(
            null,           // categoryId - 변경 안함
            null,           // sku - 변경 안함
            "새로운 상품명", // name - 변경
            null,           // description - 변경 안함
            null,           // shortDescription - 변경 안함
            "새로운 브랜드", // brand - 변경
            null,           // model - 변경 안함
            null,           // price - 변경 안함
            null,           // comparePrice - 변경 안함
            null,           // costPrice - 변경 안함
            null,           // weight - 변경 안함
            null,           // productAttributes - 변경 안함
            null,           // visibility - 변경 안함
            null,           // taxClass - 변경 안함
            null,           // metaTitle - 변경 안함
            null,           // metaDescription - 변경 안함
            null,           // searchKeywords - 변경 안함
            null            // isFeatured - 변경 안함
        );

        // then
        assertThat(product.getName()).isEqualTo("새로운 상품명");
        assertThat(product.getBrand()).isEqualTo("새로운 브랜드");

        // 변경되지 않은 필드들 확인
        assertThat(product.getSku()).isEqualTo(originalSku);
        assertThat(product.getDescription()).isEqualTo(originalDescription);
        assertThat(product.getPrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("부분 업데이트 - null 필드는 변경되지 않음")
    void updatePartially_NullFields_NoChange() {
        // given
        String originalName = product.getName();
        BigDecimal originalPrice = product.getPrice();

        // when - 모든 필드를 null로 업데이트 시도
        product.updatePartially(
            null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null
        );

        // then
        assertThat(product.getName()).isEqualTo(originalName);
        assertThat(product.getPrice()).isEqualTo(originalPrice);
    }

    @Test
    @DisplayName("업데이트 가능 상태 확인 - ACTIVE 상품")
    void isUpdatable_ActiveProduct_ReturnsTrue() {
        // given
        Product activeProduct = Product.reconstitute(
            1L, ProductCategory.ELECTRONICS.getId(), "SKU", "이름", "설명",
            null, null, null, new BigDecimal("10000"), null, null, null, null,
            ProductStatus.ACTIVE, "PUBLIC", null, null, null, null, false,
            LocalDateTime.now(), LocalDateTime.now()
        );

        // when & then
        assertThat(activeProduct.isUpdatable()).isTrue();
    }

    @Test
    @DisplayName("업데이트 가능 상태 확인 - ARCHIVED 상품")
    void isUpdatable_ArchivedProduct_ReturnsFalse() {
        // given
        Product archivedProduct = Product.reconstitute(
            1L, ProductCategory.ELECTRONICS.getId(), "SKU", "이름", "설명",
            null, null, null, new BigDecimal("10000"), null, null, null, null,
            ProductStatus.ARCHIVED, "PUBLIC", null, null, null, null, false,
            LocalDateTime.now(), LocalDateTime.now()
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
        assertThat(product.getPrice()).isEqualTo(newPrice);
    }

}
