package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductViewCountPort productViewCountPort;

    @Mock
    private ProductSearchMapper productSearchMapper;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Product testProduct1;

    private Product testProduct2;

    private ProductSearchCommand searchCommand;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.reconstitute(
            1L,                                // id
            "TEST-001",                       // sku
            "Test Product 1",                // name
            "Short Description 1",           // shortDescription
            "Test Description 1",            // description
            1L,                               // categoryId
            "TestBrand",                     // brand
            ProductType.PHYSICAL,             // productType
            ProductStatus.ACTIVE,             // status
            new BigDecimal("100.00"),         // basePrice
            new BigDecimal("120.00"),         // salePrice
            "KRW",                           // currency
            1000,                             // weightGrams
            true,                             // requiresShipping
            true,                             // isTaxable
            true,                             // isFeatured
            "test-product-1",                // slug
            "test keywords",                 // searchTags
            null,                             // primaryImageUrl
            1,                                // minOrderQuantity
            100,                              // maxOrderQuantity
            LocalDateTime.now().minusDays(1), // createdAt
            LocalDateTime.now(),              // updatedAt
            null,                         // deletedAt
            1L                                // version
        );

        testProduct2 = Product.reconstitute(
            2L,                                // id
            "TEST-002",                       // sku
            "Test Product 2",                // name
            "Short Description 2",           // shortDescription
            "Test Description 2",            // description
            1L,                               // categoryId
            "TestBrand",                     // brand
            ProductType.PHYSICAL,             // productType
            ProductStatus.ACTIVE,             // status
            new BigDecimal("200.00"),         // basePrice
            new BigDecimal("250.00"),         // salePrice
            "KRW",                           // currency
            2000,                             // weightGrams
            true,                             // requiresShipping
            true,                             // isTaxable
            false,                            // isFeatured
            "test-product-2",                // slug
            "test keywords",                 // searchTags
            null,                             // primaryImageUrl
            1,                                // minOrderQuantity
            100,                              // maxOrderQuantity
            LocalDateTime.now().minusDays(2), // createdAt
            LocalDateTime.now(),              // updatedAt
            null,                         // deletedAt
            1L                                // version
        );

        searchCommand = ProductSearchCommand.builder()
            .categoryId(1L)
            .minPrice(new BigDecimal("50.00"))
            .maxPrice(new BigDecimal("300.00"))
            .status(ProductStatus.ACTIVE)
            .page(0)
            .size(20)
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
            .build();
    }

    @Test
    @DisplayName("상품 검색이 정상적으로 수행된다")
    void searchProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products);

        ProductSearchResponse response1 = ProductSearchResponse.builder()
            .id(1L)
            .sku("TEST-001")
            .name("Test Product 1")
            .shortDescription("Short Description 1")
            .description("Test Description 1")
            .categoryId(1L)
            .brand("TestBrand")
            .productType(ProductType.PHYSICAL)
            .status(ProductStatus.ACTIVE)
            .basePrice(new BigDecimal("100.00"))
            .salePrice(new BigDecimal("120.00"))
            .currency("KRW")
            .weightGrams(1000)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(true)
            .slug("test-product-1")
            .searchTags("test keywords")
            .version(1L)
            .viewCount(5L)
            .build();

        ProductSearchResponse response2 = ProductSearchResponse.builder()
            .id(2L)
            .sku("TEST-002")
            .name("Test Product 2")
            .shortDescription("Short Description 2")
            .description("Test Description 2")
            .categoryId(1L)
            .brand("TestBrand")
            .productType(ProductType.PHYSICAL)
            .status(ProductStatus.ACTIVE)
            .basePrice(new BigDecimal("200.00"))
            .salePrice(new BigDecimal("250.00"))
            .currency("KRW")
            .weightGrams(2000)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("test-product-2")
            .searchTags("test keywords")
            .version(1L)
            .viewCount(5L)
            .build();

        Page<ProductSearchResponse> responsePage = new PageImpl<>(Arrays.asList(response1, response2));

        ProductPageResponse pageResponse = ProductPageResponse.builder()
            .content(Arrays.asList(response1, response2))
            .page(0)
            .size(20)
            .totalElements(2L)
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        given(productRepository.searchProducts(any(ProductSearchCommand.class))).willReturn(productPage);
        given(productViewCountPort.getViewCount(anyLong())).willReturn(5L);
        given(productSearchMapper.toSearchResponse(any(Product.class))).willReturn(response1, response2);
        given(productSearchMapper.toPageResponse(any())).willReturn(pageResponse);

        // When
        ProductPageResponse result = productSearchService.searchProducts(searchCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();

        verify(productRepository).searchProducts(searchCommand);
        verify(productViewCountPort).getViewCount(1L);
        verify(productViewCountPort).getViewCount(2L);
    }

    @Test
    @DisplayName("빈 검색 결과도 정상적으로 처리된다")
    void searchProducts_EmptyResult() {
        // Given
        Page<Product> emptyPage = new PageImpl<>(Arrays.asList());
        ProductPageResponse emptyPageResponse = ProductPageResponse.builder()
            .content(Arrays.asList())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        given(productRepository.searchProducts(any(ProductSearchCommand.class))).willReturn(emptyPage);
        given(productSearchMapper.toPageResponse(any())).willReturn(emptyPageResponse);

        // When
        ProductPageResponse result = productSearchService.searchProducts(searchCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);

        verify(productRepository).searchProducts(searchCommand);
    }

    @Test
    @DisplayName("유효하지 않은 페이지 번호로 검색 시 예외가 발생한다")
    void searchProducts_InvalidPage() {
        // Given
        ProductSearchCommand invalidCommand = ProductSearchCommand.builder()
            .page(-1)
            .size(20)
            .build();

        // When & Then
        assertThatThrownBy(() -> productSearchService.searchProducts(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Page must be greater than or equal to 0");
    }

    @Test
    @DisplayName("유효하지 않은 페이지 크기로 검색 시 예외가 발생한다")
    void searchProducts_InvalidSize() {
        // Given
        ProductSearchCommand invalidCommand = ProductSearchCommand.builder()
            .page(0)
            .size(101)
            .build();

        // When & Then
        assertThatThrownBy(() -> productSearchService.searchProducts(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Size must be between 1 and 100");
    }

    @Test
    @DisplayName("유효하지 않은 가격 범위로 검색 시 예외가 발생한다")
    void searchProducts_InvalidPriceRange() {
        // Given
        ProductSearchCommand invalidCommand = ProductSearchCommand.builder()
            .minPrice(new BigDecimal("200.00"))
            .maxPrice(new BigDecimal("100.00"))
            .page(0)
            .size(20)
            .build();

        // When & Then
        assertThatThrownBy(() -> productSearchService.searchProducts(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Minimum price cannot be greater than maximum price");
    }

}
