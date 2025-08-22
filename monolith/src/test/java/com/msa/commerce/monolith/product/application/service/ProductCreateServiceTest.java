package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCreateService 테스트")
class ProductCreateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductInventoryRepository productInventoryRepository;

    @Mock
    private ProductResponseMapper productResponseMapper;

    @InjectMocks
    private ProductCreateService productCreateService;

    private ProductCreateCommand validCommand;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        validCommand = ProductCreateCommand.builder()
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .price(new BigDecimal("10000"))
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST-1234")
            .build();

        savedProduct = Product.reconstitute(
            1L,                                    // id
            ProductCategory.ELECTRONICS.getId(),   // categoryId
            "TEST1234",                           // sku
            "테스트 상품",                         // name
            "테스트 상품 설명",                    // description
            null,                                 // shortDescription
            null,                                 // brand
            null,                                 // model
            new BigDecimal("10000"),              // price
            null,                                 // comparePrice
            null,                                 // costPrice
            null,                                 // weight
            null,                                 // productAttributes
            ProductStatus.DRAFT,                  // status
            "PUBLIC",                             // visibility
            null,                                 // taxClass
            null,                                 // metaTitle
            null,                                 // metaDescription
            null,                                 // searchKeywords
            false,                                // isFeatured
            LocalDateTime.now(),                  // createdAt
            LocalDateTime.now()                   // updatedAt
        );
    }

    @Test
    @DisplayName("정상적인 상품 생성")
    void createProduct_Success() {
        // given
        ProductResponse expectedResponse = createProductResponse();
        given(productRepository.existsBySku(validCommand.getSku())).willReturn(false);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        given(productResponseMapper.toResponse(savedProduct)).willReturn(expectedResponse);

        // when
        ProductResponse response = productCreateService.createProduct(validCommand);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(validCommand.getName());
        assertThat(response.getDescription()).isEqualTo(validCommand.getDescription());
        assertThat(response.getPrice()).isEqualTo(validCommand.getPrice());
        assertThat(response.getCategoryId()).isEqualTo(validCommand.getCategoryId());
        assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);

        verify(productRepository).existsBySku(validCommand.getSku());
        verify(productRepository).save(any(Product.class));
        verify(productResponseMapper).toResponse(savedProduct);
    }

    @Test
    @DisplayName("중복된 SKU로 생성 시 예외 발생")
    void createProduct_DuplicateSku_ThrowsException() {
        // given
        given(productRepository.existsBySku(validCommand.getSku())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(validCommand))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Product SKU already exists: " + validCommand.getSku());

        verify(productRepository).existsBySku(validCommand.getSku());
        verify(productRepository, never()).save(any(Product.class));
        verify(productResponseMapper, never()).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("유효하지 않은 명령으로 생성 시 예외 발생")
    void createProduct_InvalidCommand_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
            .name("") // 빈 문자열
            .price(new BigDecimal("10000"))
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST-1234")
            .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name is required.");

        verify(productRepository, never()).existsBySku(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("가격이 null인 명령으로 생성 시 예외 발생")
    void createProduct_NullPrice_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
            .name("테스트 상품")
            .price(null) // null 가격
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST-1234")
            .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price is required.");

        verify(productRepository, never()).existsBySku(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("카테고리 ID가 null인 명령으로 생성 시 예외 발생")
    void createProduct_NullCategoryId_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
            .name("테스트 상품")
            .price(new BigDecimal("10000"))
            .categoryId(null) // null 카테고리 ID
            .sku("TEST-1234")
            .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Category ID is required.");

        verify(productRepository, never()).existsBySku(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST1234")
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .price(new BigDecimal("10000"))
            .status(ProductStatus.DRAFT)
            .visibility("PUBLIC")
            .isFeatured(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

}
