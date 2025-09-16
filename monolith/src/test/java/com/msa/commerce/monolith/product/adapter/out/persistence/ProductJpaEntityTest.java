package com.msa.commerce.monolith.product.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

class ProductJpaEntityTest {

    @Test
    @DisplayName("도메인 엔티티를 JPA 엔티티로 변환할 수 있다")
    void fromDomainEntity_ShouldMapCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Product domainProduct = Product.reconstitute(
            1L,                                    // id
            "TEST-SKU-001",                       // sku
            "Test Product",                       // name
            "Short description",                  // shortDescription
            "Test Description",                   // description
            10L,                                  // categoryId
            "TestBrand",                         // brand
            ProductType.PHYSICAL,                // productType
            ProductStatus.ACTIVE,                // status
            new BigDecimal("29.99"),             // basePrice
            new BigDecimal("25.99"),             // salePrice
            "KRW",                               // currency
            1500,                                // weightGrams
            true,                                // requiresShipping
            true,                                // isTaxable
            true,                                // isFeatured
            "test-product",                      // slug
            "test, product, electronics",        // searchTags
            "https://example.com/image.jpg",     // primaryImageUrl
            1,                                   // minOrderQuantity
            100,                                 // maxOrderQuantity
            now,                                 // createdAt
            now,                                 // updatedAt
            1L                                   // version
        );

        // when
        ProductJpaEntity jpaEntity = ProductJpaEntity.fromDomainEntity(domainProduct);

        // then
        assertThat(jpaEntity.getId()).isEqualTo(1L);
        assertThat(jpaEntity.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(jpaEntity.getName()).isEqualTo("Test Product");
        assertThat(jpaEntity.getShortDescription()).isEqualTo("Short description");
        assertThat(jpaEntity.getDescription()).isEqualTo("Test Description");
        assertThat(jpaEntity.getCategoryId()).isEqualTo(10L);
        assertThat(jpaEntity.getBrand()).isEqualTo("TestBrand");
        assertThat(jpaEntity.getProductType()).isEqualTo(ProductType.PHYSICAL);
        assertThat(jpaEntity.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(jpaEntity.getBasePrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(jpaEntity.getSalePrice()).isEqualTo(new BigDecimal("25.99"));
        assertThat(jpaEntity.getCurrency()).isEqualTo("KRW");
        assertThat(jpaEntity.getWeightGrams()).isEqualTo(1500);
        assertThat(jpaEntity.getRequiresShipping()).isTrue();
        assertThat(jpaEntity.getIsTaxable()).isTrue();
        assertThat(jpaEntity.getIsFeatured()).isTrue();
        assertThat(jpaEntity.getSlug()).isEqualTo("test-product");
        assertThat(jpaEntity.getSearchTags()).isEqualTo("test, product, electronics");
        assertThat(jpaEntity.getPrimaryImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(jpaEntity.getCreatedAt()).isEqualTo(now);
        assertThat(jpaEntity.getUpdatedAt()).isEqualTo(now);
        assertThat(jpaEntity.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("새로운 도메인 엔티티를 JPA 엔티티로 변환할 수 있다 (ID 없음)")
    void fromDomainEntityForCreation_ShouldMapCorrectly() {
        // given
        Product domainProduct = Product.builder()
            .sku("NEW-SKU-001")
            .name("New Product")
            .shortDescription("New short description")
            .description("New Description")
            .categoryId(5L)
            .brand("NewBrand")
            .productType(ProductType.DIGITAL)
            .basePrice(new BigDecimal("19.99"))
            .salePrice(new BigDecimal("15.99"))
            .currency("USD")
            .weightGrams(null)
            .requiresShipping(false)
            .isTaxable(true)
            .isFeatured(false)
            .slug("new-product")
            .searchTags("new, product, test")
            .primaryImageUrl("https://example.com/new-image.jpg")
            .build();

        // when
        ProductJpaEntity jpaEntity = ProductJpaEntity.fromDomainEntityForCreation(domainProduct);

        // then
        assertThat(jpaEntity.getId()).isNull();
        assertThat(jpaEntity.getSku()).isEqualTo("NEW-SKU-001");
        assertThat(jpaEntity.getName()).isEqualTo("New Product");
        assertThat(jpaEntity.getShortDescription()).isEqualTo("New short description");
        assertThat(jpaEntity.getDescription()).isEqualTo("New Description");
        assertThat(jpaEntity.getCategoryId()).isEqualTo(5L);
        assertThat(jpaEntity.getBrand()).isEqualTo("NewBrand");
        assertThat(jpaEntity.getProductType()).isEqualTo(ProductType.DIGITAL);
        assertThat(jpaEntity.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(jpaEntity.getBasePrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(jpaEntity.getSalePrice()).isEqualTo(new BigDecimal("15.99"));
        assertThat(jpaEntity.getCurrency()).isEqualTo("USD");
        assertThat(jpaEntity.getWeightGrams()).isNull();
        assertThat(jpaEntity.getRequiresShipping()).isFalse();
        assertThat(jpaEntity.getIsTaxable()).isTrue();
        assertThat(jpaEntity.getIsFeatured()).isFalse();
        assertThat(jpaEntity.getSlug()).isEqualTo("new-product");
        assertThat(jpaEntity.getSearchTags()).isEqualTo("new, product, test");
        assertThat(jpaEntity.getPrimaryImageUrl()).isEqualTo("https://example.com/new-image.jpg");
    }

    @Test
    @DisplayName("JPA 엔티티를 도메인 엔티티로 변환할 수 있다")
    void toDomainEntity_ShouldMapCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        setField(jpaEntity, "id", 2L);
        setField(jpaEntity, "sku", "JPA-SKU-002");
        setField(jpaEntity, "name", "JPA Product");
        setField(jpaEntity, "shortDescription", "JPA Short description");
        setField(jpaEntity, "description", "JPA Description");
        setField(jpaEntity, "categoryId", 15L);
        setField(jpaEntity, "brand", "JPABrand");
        setField(jpaEntity, "productType", ProductType.SERVICE);
        setField(jpaEntity, "status", ProductStatus.INACTIVE);
        setField(jpaEntity, "basePrice", new BigDecimal("39.99"));
        setField(jpaEntity, "salePrice", new BigDecimal("29.99"));
        setField(jpaEntity, "currency", "EUR");
        setField(jpaEntity, "weightGrams", 2100);
        setField(jpaEntity, "requiresShipping", true);
        setField(jpaEntity, "isTaxable", false);
        setField(jpaEntity, "isFeatured", true);
        setField(jpaEntity, "slug", "jpa-product");
        setField(jpaEntity, "searchTags", "jpa, test, product");
        setField(jpaEntity, "primaryImageUrl", "https://example.com/jpa-image.jpg");
        setField(jpaEntity, "createdAt", now);
        setField(jpaEntity, "updatedAt", now);
        setField(jpaEntity, "version", 2L);

        // when
        Product domainProduct = jpaEntity.toDomainEntity();

        // then
        assertThat(domainProduct.getId()).isEqualTo(2L);
        assertThat(domainProduct.getSku()).isEqualTo("JPA-SKU-002");
        assertThat(domainProduct.getName()).isEqualTo("JPA Product");
        assertThat(domainProduct.getShortDescription()).isEqualTo("JPA Short description");
        assertThat(domainProduct.getDescription()).isEqualTo("JPA Description");
        assertThat(domainProduct.getCategoryId()).isEqualTo(15L);
        assertThat(domainProduct.getBrand()).isEqualTo("JPABrand");
        assertThat(domainProduct.getProductType()).isEqualTo(ProductType.SERVICE);
        assertThat(domainProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(domainProduct.getBasePrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(domainProduct.getSalePrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(domainProduct.getCurrency()).isEqualTo("EUR");
        assertThat(domainProduct.getWeightGrams()).isEqualTo(2100);
        assertThat(domainProduct.getRequiresShipping()).isTrue();
        assertThat(domainProduct.getIsTaxable()).isFalse();
        assertThat(domainProduct.getIsFeatured()).isTrue();
        assertThat(domainProduct.getSlug()).isEqualTo("jpa-product");
        assertThat(domainProduct.getSearchTags()).isEqualTo("jpa, test, product");
        assertThat(domainProduct.getPrimaryImageUrl()).isEqualTo("https://example.com/jpa-image.jpg");
        assertThat(domainProduct.getCreatedAt()).isEqualTo(now);
        assertThat(domainProduct.getUpdatedAt()).isEqualTo(now);
        assertThat(domainProduct.getVersion()).isEqualTo(2L);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

}
