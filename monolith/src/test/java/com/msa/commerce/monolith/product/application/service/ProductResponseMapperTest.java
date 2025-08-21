package com.msa.commerce.monolith.product.application.service;

import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseMapperTest {

    private ProductResponseMapper productResponseMapper;
    private Product product;

    @BeforeEach
    void setUp() {
        productResponseMapper = new ProductResponseMapper();
        
        // Product.reconstitute를 사용하여 테스트용 Product 객체 생성
        product = Product.reconstitute(
                1L,                                    // id
                ProductCategory.ELECTRONICS.getId(),   // categoryId
                "TEST1234",                           // sku
                "Test Product",                       // name
                "Test Description",                   // description
                "Short description",                  // shortDescription
                "Test Brand",                        // brand
                "Test Model",                        // model
                new BigDecimal("29.99"),             // price
                new BigDecimal("39.99"),             // comparePrice
                new BigDecimal("19.99"),             // costPrice
                new BigDecimal("1.5"),               // weight
                "{\"color\": \"red\"}",              // productAttributes
                ProductStatus.ACTIVE,                // status
                "PUBLIC",                            // visibility
                "STANDARD",                          // taxClass
                "Test Meta Title",                   // metaTitle
                "Test Meta Description",             // metaDescription
                "test, product, electronics",       // searchKeywords
                false,                               // isFeatured
                LocalDateTime.now(),                 // createdAt
                LocalDateTime.now()                  // updatedAt
        );
    }

    @Test
    @DisplayName("Product 엔티티를 ProductResponse로 변환할 수 있다")
    void toResponse_ShouldMapEntityToResponse() {
        // when
        ProductResponse response = productResponseMapper.toResponse(product);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategoryId()).isEqualTo(ProductCategory.ELECTRONICS.getId());
        assertThat(response.getSku()).isEqualTo("TEST1234");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getShortDescription()).isEqualTo("Short description");
        assertThat(response.getBrand()).isEqualTo("Test Brand");
        assertThat(response.getModel()).isEqualTo("Test Model");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(response.getComparePrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(response.getCostPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(response.getWeight()).isEqualTo(new BigDecimal("1.5"));
        assertThat(response.getProductAttributes()).isEqualTo("{\"color\": \"red\"}");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.getVisibility()).isEqualTo("PUBLIC");
        assertThat(response.getTaxClass()).isEqualTo("STANDARD");
        assertThat(response.getMetaTitle()).isEqualTo("Test Meta Title");
        assertThat(response.getMetaDescription()).isEqualTo("Test Meta Description");
        assertThat(response.getSearchKeywords()).isEqualTo("test, product, electronics");
        assertThat(response.getIsFeatured()).isFalse();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Product에서 null 값이 있어도 Response로 변환할 수 있다")
    void toResponse_ShouldHandleNullValues() {
        // given
        Product productWithNulls = Product.reconstitute(
                2L,                                  // id
                ProductCategory.BOOKS.getId(),       // categoryId
                "BOOK1234",                         // sku
                "Test Product",                     // name
                null,                               // description null
                null,                               // shortDescription null
                null,                               // brand null
                null,                               // model null
                new BigDecimal("19.99"),           // price
                null,                               // comparePrice null
                null,                               // costPrice null
                null,                               // weight null
                null,                               // productAttributes null
                ProductStatus.ACTIVE,               // status
                "PUBLIC",                           // visibility
                null,                               // taxClass null
                null,                               // metaTitle null
                null,                               // metaDescription null
                null,                               // searchKeywords null
                true,                               // isFeatured
                LocalDateTime.now(),                // createdAt
                LocalDateTime.now()                 // updatedAt
        );

        // when
        ProductResponse response = productResponseMapper.toResponse(productWithNulls);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getCategoryId()).isEqualTo(ProductCategory.BOOKS.getId());
        assertThat(response.getSku()).isEqualTo("BOOK1234");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getShortDescription()).isNull();
        assertThat(response.getBrand()).isNull();
        assertThat(response.getModel()).isNull();
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(response.getComparePrice()).isNull();
        assertThat(response.getCostPrice()).isNull();
        assertThat(response.getWeight()).isNull();
        assertThat(response.getProductAttributes()).isNull();
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.getVisibility()).isEqualTo("PUBLIC");
        assertThat(response.getTaxClass()).isNull();
        assertThat(response.getMetaTitle()).isNull();
        assertThat(response.getMetaDescription()).isNull();
        assertThat(response.getSearchKeywords()).isNull();
        assertThat(response.getIsFeatured()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("null Product는 null Response를 반환한다")
    void toResponse_ShouldReturnNullForNullProduct() {
        // when
        ProductResponse response = productResponseMapper.toResponse(null);

        // then
        assertThat(response).isNull();
    }
}