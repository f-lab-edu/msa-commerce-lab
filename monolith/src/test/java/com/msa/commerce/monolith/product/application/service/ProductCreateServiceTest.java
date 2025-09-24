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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.command.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.fixture.ProductCommandFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCreateService 테스트")
class ProductCreateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ProductCreateService productCreateService;

    private ProductCreateCommand validCommand;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        validCommand = ProductCommandFixture.validProductCreateCommand();

        savedProduct = Product.reconstitute(
            1L,                                    // id
            "TEST-1234",                         // sku
            "테스트 상품",                         // name
            null,                                 // shortDescription
            "테스트 상품 설명",                    // description
            1L,                                   // categoryId
            null,                                 // brand
            ProductType.PHYSICAL,                 // productType
            ProductStatus.DRAFT,                  // status
            new BigDecimal("10000"),              // basePrice
            null,                                 // salePrice
            "KRW",                               // currency
            null,                                 // weightGrams
            true,                                 // requiresShipping
            true,                                 // isTaxable
            false,                                // isFeatured
            "test-product",                      // slug
            null,                                 // searchTags
            null,                                 // primaryImageUrl
            1,                                    // minOrderQuantity
            100,                                  // maxOrderQuantity
            LocalDateTime.now(),                  // createdAt
            LocalDateTime.now(),                  // updatedAt
            null,                                 // deletedAt
            1L                                    // version
        );
    }

    @Test
    @DisplayName("정상적인 상품 생성")
    void createProduct_Success() {
        // given
        ProductResponse expectedResponse = createProductResponse();
        given(productRepository.existsBySku(validCommand.getSku())).willReturn(false);
        given(productMapper.toProduct(validCommand)).willReturn(savedProduct);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);
        given(productMapper.toResponse(savedProduct)).willReturn(expectedResponse);

        // when
        ProductResponse response = productCreateService.createProduct(validCommand);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(validCommand.getName());
        assertThat(response.getDescription()).isEqualTo(validCommand.getDescription());
        assertThat(response.getBasePrice()).isEqualTo(validCommand.getBasePrice());
        assertThat(response.getCategoryId()).isEqualTo(validCommand.getCategoryId());
        assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);

        verify(productRepository).existsBySku(validCommand.getSku());
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toResponse(savedProduct);
    }

    @Test
    @DisplayName("중복된 SKU로 생성 시 예외 발생")
    void createProduct_DuplicateSku_ThrowsException() {
        // given
        ProductCreateCommand duplicateSkuCommand = ProductCommandFixture.duplicateSkuCommand();
        given(productRepository.existsBySku(duplicateSkuCommand.getSku())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(duplicateSkuCommand))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Product SKU already exists: " + duplicateSkuCommand.getSku());

        verify(productRepository).existsBySku(duplicateSkuCommand.getSku());
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @ParameterizedTest(name = "{0} - 유효하지 않은 명령으로 생성 시 예외 발생")
    @MethodSource("com.msa.commerce.monolith.product.fixture.ProductCommandFixture#invalidCreateCommandScenarios")
    @DisplayName("유효하지 않은 명령으로 생성 시 예외 발생")
    void createProduct_InvalidCommand_ThrowsException(String scenario, ProductCreateCommand invalidCommand) {
        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).existsBySku(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .sku("TEST-1234")
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("10000"))
            .currency("KRW")
            .status(ProductStatus.DRAFT)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("test-product")
            .version(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

}
