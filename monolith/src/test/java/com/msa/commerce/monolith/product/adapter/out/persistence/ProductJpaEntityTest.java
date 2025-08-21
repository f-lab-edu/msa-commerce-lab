package com.msa.commerce.monolith.product.adapter.out.persistence;

import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductJpaEntityTest {

    @Test
    @DisplayName("도메인 엔티티를 JPA 엔티티로 변환할 수 있다")
    void fromDomainEntity_ShouldMapCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Product domainProduct = Product.reconstitute(
                1L,
                10L,                                    // categoryId
                "TEST-SKU-001",                        // sku
                "Test Product",                        // name
                "Test Description",                    // description
                "Short description",                   // shortDescription
                "TestBrand",                          // brand
                "TestModel",                          // model
                new BigDecimal("29.99"),              // price
                new BigDecimal("39.99"),              // comparePrice
                new BigDecimal("19.99"),              // costPrice
                new BigDecimal("1.5"),                // weight
                "{\"color\": \"red\"}",               // productAttributes
                ProductStatus.ACTIVE,                 // status
                "PUBLIC",                             // visibility
                "STANDARD",                           // taxClass
                "Test Meta Title",                    // metaTitle
                "Test meta description",              // metaDescription
                "test, product, electronics",        // searchKeywords
                true,                                 // isFeatured
                now,                                  // createdAt
                now                                   // updatedAt
        );

        // when
        ProductJpaEntity jpaEntity = ProductJpaEntity.fromDomainEntity(domainProduct);

        // then
        assertThat(jpaEntity.getId()).isEqualTo(1L);
        assertThat(jpaEntity.getCategoryId()).isEqualTo(10L);
        assertThat(jpaEntity.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(jpaEntity.getName()).isEqualTo("Test Product");
        assertThat(jpaEntity.getDescription()).isEqualTo("Test Description");
        assertThat(jpaEntity.getShortDescription()).isEqualTo("Short description");
        assertThat(jpaEntity.getBrand()).isEqualTo("TestBrand");
        assertThat(jpaEntity.getModel()).isEqualTo("TestModel");
        assertThat(jpaEntity.getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(jpaEntity.getComparePrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(jpaEntity.getCostPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(jpaEntity.getWeight()).isEqualTo(new BigDecimal("1.5"));
        assertThat(jpaEntity.getProductAttributes()).isEqualTo("{\"color\": \"red\"}");
        assertThat(jpaEntity.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(jpaEntity.getVisibility()).isEqualTo("PUBLIC");
        assertThat(jpaEntity.getTaxClass()).isEqualTo("STANDARD");
        assertThat(jpaEntity.getMetaTitle()).isEqualTo("Test Meta Title");
        assertThat(jpaEntity.getMetaDescription()).isEqualTo("Test meta description");
        assertThat(jpaEntity.getSearchKeywords()).isEqualTo("test, product, electronics");
        assertThat(jpaEntity.getIsFeatured()).isTrue();
        assertThat(jpaEntity.getCreatedAt()).isEqualTo(now);
        assertThat(jpaEntity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("새로운 도메인 엔티티를 JPA 엔티티로 변환할 수 있다 (ID 없음)")
    void fromDomainEntityForCreation_ShouldMapCorrectly() {
        // given
        Product domainProduct = Product.builder()
                .categoryId(5L)
                .sku("NEW-SKU-001")
                .name("New Product")
                .description("New Description")
                .shortDescription("New short description")
                .brand("NewBrand")
                .model("NewModel")
                .price(new BigDecimal("19.99"))
                .comparePrice(new BigDecimal("24.99"))
                .costPrice(new BigDecimal("12.99"))
                .weight(new BigDecimal("0.8"))
                .productAttributes("{\"size\": \"M\"}")
                .visibility("PUBLIC")
                .taxClass("STANDARD")
                .metaTitle("New Meta Title")
                .metaDescription("New meta description")
                .searchKeywords("new, product, test")
                .isFeatured(false)
                .build();

        // when
        ProductJpaEntity jpaEntity = ProductJpaEntity.fromDomainEntityForCreation(domainProduct);

        // then
        assertThat(jpaEntity.getId()).isNull();
        assertThat(jpaEntity.getCategoryId()).isEqualTo(5L);
        assertThat(jpaEntity.getSku()).isEqualTo("NEW-SKU-001");
        assertThat(jpaEntity.getName()).isEqualTo("New Product");
        assertThat(jpaEntity.getDescription()).isEqualTo("New Description");
        assertThat(jpaEntity.getShortDescription()).isEqualTo("New short description");
        assertThat(jpaEntity.getBrand()).isEqualTo("NewBrand");
        assertThat(jpaEntity.getModel()).isEqualTo("NewModel");
        assertThat(jpaEntity.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(jpaEntity.getComparePrice()).isEqualTo(new BigDecimal("24.99"));
        assertThat(jpaEntity.getCostPrice()).isEqualTo(new BigDecimal("12.99"));
        assertThat(jpaEntity.getWeight()).isEqualTo(new BigDecimal("0.8"));
        assertThat(jpaEntity.getProductAttributes()).isEqualTo("{\"size\": \"M\"}");
        assertThat(jpaEntity.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(jpaEntity.getVisibility()).isEqualTo("PUBLIC");
        assertThat(jpaEntity.getTaxClass()).isEqualTo("STANDARD");
        assertThat(jpaEntity.getMetaTitle()).isEqualTo("New Meta Title");
        assertThat(jpaEntity.getMetaDescription()).isEqualTo("New meta description");
        assertThat(jpaEntity.getSearchKeywords()).isEqualTo("new, product, test");
        assertThat(jpaEntity.getIsFeatured()).isFalse();
    }

    @Test
    @DisplayName("JPA 엔티티를 도메인 엔티티로 변환할 수 있다")
    void toDomainEntity_ShouldMapCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        ProductJpaEntity jpaEntity = new ProductJpaEntity();
        setField(jpaEntity, "id", 2L);
        setField(jpaEntity, "categoryId", 15L);
        setField(jpaEntity, "sku", "JPA-SKU-002");
        setField(jpaEntity, "name", "JPA Product");
        setField(jpaEntity, "description", "JPA Description");
        setField(jpaEntity, "shortDescription", "JPA Short description");
        setField(jpaEntity, "brand", "JPABrand");
        setField(jpaEntity, "model", "JPAModel");
        setField(jpaEntity, "price", new BigDecimal("39.99"));
        setField(jpaEntity, "comparePrice", new BigDecimal("49.99"));
        setField(jpaEntity, "costPrice", new BigDecimal("29.99"));
        setField(jpaEntity, "weight", new BigDecimal("2.1"));
        setField(jpaEntity, "productAttributes", "{\"style\": \"modern\"}");
        setField(jpaEntity, "status", ProductStatus.INACTIVE);
        setField(jpaEntity, "visibility", "PRIVATE");
        setField(jpaEntity, "taxClass", "PREMIUM");
        setField(jpaEntity, "metaTitle", "JPA Meta Title");
        setField(jpaEntity, "metaDescription", "JPA meta description");
        setField(jpaEntity, "searchKeywords", "jpa, test, product");
        setField(jpaEntity, "isFeatured", true);
        setField(jpaEntity, "createdAt", now);
        setField(jpaEntity, "updatedAt", now);

        // when
        Product domainProduct = jpaEntity.toDomainEntity();

        // then
        assertThat(domainProduct.getId()).isEqualTo(2L);
        assertThat(domainProduct.getCategoryId()).isEqualTo(15L);
        assertThat(domainProduct.getSku()).isEqualTo("JPA-SKU-002");
        assertThat(domainProduct.getName()).isEqualTo("JPA Product");
        assertThat(domainProduct.getDescription()).isEqualTo("JPA Description");
        assertThat(domainProduct.getShortDescription()).isEqualTo("JPA Short description");
        assertThat(domainProduct.getBrand()).isEqualTo("JPABrand");
        assertThat(domainProduct.getModel()).isEqualTo("JPAModel");
        assertThat(domainProduct.getPrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(domainProduct.getComparePrice()).isEqualTo(new BigDecimal("49.99"));
        assertThat(domainProduct.getCostPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(domainProduct.getWeight()).isEqualTo(new BigDecimal("2.1"));
        assertThat(domainProduct.getProductAttributes()).isEqualTo("{\"style\": \"modern\"}");
        assertThat(domainProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(domainProduct.getVisibility()).isEqualTo("PRIVATE");
        assertThat(domainProduct.getTaxClass()).isEqualTo("PREMIUM");
        assertThat(domainProduct.getMetaTitle()).isEqualTo("JPA Meta Title");
        assertThat(domainProduct.getMetaDescription()).isEqualTo("JPA meta description");
        assertThat(domainProduct.getSearchKeywords()).isEqualTo("jpa, test, product");
        assertThat(domainProduct.getIsFeatured()).isTrue();
        assertThat(domainProduct.getCreatedAt()).isEqualTo(now);
        assertThat(domainProduct.getUpdatedAt()).isEqualTo(now);
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