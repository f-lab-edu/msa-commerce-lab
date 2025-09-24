package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@DisplayName("ProductMapper - MapStruct 매핑 테스트")
class ProductMapperTest {

    private ProductMapper productMapper;

    private Product product;

    @BeforeEach
    void setUp() {
        // MapStruct에서 생성된 구현체를 직접 사용
        productMapper = Mappers.getMapper(ProductMapper.class);

        // Product.reconstitute를 사용하여 테스트용 Product 객체 생성
        product = Product.reconstitute(
            1L,                                    // id
            "TEST1234",                           // sku
            "Test Product",                       // name
            "Short description",                  // shortDescription
            "Test Description",                   // description
            1L,                                   // categoryId
            "Test Brand",                        // brand
            ProductType.PHYSICAL,                 // productType
            ProductStatus.ACTIVE,                 // status
            new BigDecimal("29.99"),             // basePrice
            new BigDecimal("39.99"),             // salePrice
            "KRW",                               // currency
            1500,                                 // weightGrams
            true,                                 // requiresShipping
            true,                                 // isTaxable
            false,                                // isFeatured
            "test-product",                      // slug
            "test, product, electronics",       // searchTags
            null,                                 // primaryImageUrl
            5,                                    // minOrderQuantity
            50,                                   // maxOrderQuantity
            LocalDateTime.now(),                  // createdAt
            LocalDateTime.now(),                  // updatedAt
            null,                         // deletedAt
            1L                                    // version
        );
    }

    @Test
    @DisplayName("Product 엔티티를 ProductResponse로 변환할 수 있다")
    void toResponse_ShouldMapEntityToResponse() {
        // when
        ProductResponse response = productMapper.toResponse(product);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("TEST1234");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getShortDescription()).isEqualTo("Short description");
        assertThat(response.getBrand()).isEqualTo("Test Brand");
        assertThat(response.getProductType()).isEqualTo(ProductType.PHYSICAL);
        assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("29.99"));
        assertThat(response.getSalePrice()).isEqualTo(new BigDecimal("39.99"));
        assertThat(response.getCurrency()).isEqualTo("KRW");
        assertThat(response.getWeightGrams()).isEqualTo(1500);
        assertThat(response.getRequiresShipping()).isTrue();
        assertThat(response.getIsTaxable()).isTrue();
        assertThat(response.getSearchTags()).isEqualTo("test, product, electronics");
        assertThat(response.getSlug()).isEqualTo("test-product");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.getIsFeatured()).isFalse();
        assertThat(response.getVersion()).isEqualTo(1L);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        // MapStruct 상수 매핑 검증 - 비즈니스 로직 테스트
        assertThat(response.getAvailableQuantity()).isEqualTo(0);
        assertThat(response.getReservedQuantity()).isEqualTo(0);
        assertThat(response.getTotalQuantity()).isEqualTo(0);
        assertThat(response.getLowStockThreshold()).isEqualTo(0);
        assertThat(response.getIsTrackingEnabled()).isTrue();
        assertThat(response.getIsBackorderAllowed()).isFalse();
        assertThat(response.getReorderPoint()).isEqualTo(10);
        assertThat(response.getReorderQuantity()).isEqualTo(20);
        assertThat(response.getLocationCode()).isEqualTo("MAIN");
    }

    @Test
    @DisplayName("MapStruct null 처리 - Product null 필드들이 올바르게 매핑된다")
    void toResponse_ShouldHandleNullValues() {
        // given
        Product productWithNulls = Product.reconstitute(
            2L,                                  // id
            "BOOK1234",                         // sku
            "Test Product",                     // name
            null,                               // shortDescription null
            null,                               // description null
            2L,                                 // categoryId
            null,                               // brand null
            ProductType.DIGITAL,                // productType
            ProductStatus.ACTIVE,               // status
            new BigDecimal("19.99"),           // basePrice
            null,                               // salePrice null
            "USD",                              // currency
            null,                               // weightGrams null
            false,                              // requiresShipping
            true,                               // isTaxable
            true,                               // isFeatured
            "test-book",                       // slug
            null,                               // searchTags null
            null,                               // primaryImageUrl null
            1,                                  // minOrderQuantity
            100,                                // maxOrderQuantity
            LocalDateTime.now(),                // createdAt
            LocalDateTime.now(),                // updatedAt
            null,                         // deletedAt
            1L                                  // version
        );

        // when
        ProductResponse response = productMapper.toResponse(productWithNulls);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getCategoryId()).isEqualTo(2L);
        assertThat(response.getSku()).isEqualTo("BOOK1234");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getShortDescription()).isNull();
        assertThat(response.getBrand()).isNull();
        assertThat(response.getProductType()).isEqualTo(ProductType.DIGITAL);
        assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("19.99"));
        assertThat(response.getSalePrice()).isNull();
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getWeightGrams()).isNull();
        assertThat(response.getRequiresShipping()).isFalse();
        assertThat(response.getIsTaxable()).isTrue();
        assertThat(response.getSearchTags()).isNull();
        assertThat(response.getSlug()).isEqualTo("test-book");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.getIsFeatured()).isTrue();
        assertThat(response.getVersion()).isEqualTo(1L);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        // MapStruct 상수 매핑이 null 값과 관계없이 동작하는지 검증
        assertThat(response.getLocationCode()).isEqualTo("MAIN");
        assertThat(response.getIsTrackingEnabled()).isTrue();
    }

    @Test
    @DisplayName("MapStruct null 안전성 - null Product 입력 시 null 반환")
    void toResponse_ShouldReturnNullForNullProduct() {
        // when
        ProductResponse response = productMapper.toResponse(null);

        // then - MapStruct의 null 안전성 검증
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("ProductSearchResponse 매핑 - viewCount 상수 매핑 검증")
    void toSearchResponse_ShouldMapWithDefaultViewCount() {
        // when
        var searchResponse = productMapper.toSearchResponse(product);

        // then - 기본 매핑 검증
        assertThat(searchResponse).isNotNull();
        assertThat(searchResponse.getId()).isEqualTo(product.getId());
        assertThat(searchResponse.getName()).isEqualTo(product.getName());

        // MapStruct 상수 매핑 검증
        assertThat(searchResponse.getViewCount()).isEqualTo(0L);
    }

}
