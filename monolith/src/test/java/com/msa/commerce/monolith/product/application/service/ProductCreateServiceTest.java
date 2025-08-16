package com.msa.commerce.monolith.product.application.service;

import com.msa.commerce.monolith.product.application.port.in.ProductCreateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ProductCreateService 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCreateService 테스트")
class ProductCreateServiceTest {

    @Mock
    private ProductRepository productRepository;

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
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .imageUrl("http://example.com/image.jpg")
                .build();

        savedProduct = Product.builder()
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .imageUrl("http://example.com/image.jpg")
                .build();
        
        // ID와 생성 시간 설정 (리플렉션을 사용하여 설정할 수도 있지만, 여기서는 간단히 처리)
        // 실제로는 테스트용 팩토리 메서드나 빌더 패턴을 사용하는 것이 좋습니다.
    }

    @Test
    @DisplayName("정상적인 상품 생성")
    void createProduct_Success() {
        // given
        given(productRepository.existsByName(validCommand.getName())).willReturn(false);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        ProductResponse response = productCreateService.createProduct(validCommand);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(validCommand.getName());
        assertThat(response.getDescription()).isEqualTo(validCommand.getDescription());
        assertThat(response.getPrice()).isEqualTo(validCommand.getPrice());
        assertThat(response.getStockQuantity()).isEqualTo(validCommand.getStockQuantity());
        assertThat(response.getCategory()).isEqualTo(validCommand.getCategory());
        assertThat(response.getImageUrl()).isEqualTo(validCommand.getImageUrl());
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);

        verify(productRepository).existsByName(validCommand.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("중복된 상품명으로 생성 시 예외 발생")
    void createProduct_DuplicateName_ThrowsException() {
        // given
        given(productRepository.existsByName(validCommand.getName())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(validCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 상품명입니다: " + validCommand.getName());

        verify(productRepository).existsByName(validCommand.getName());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("유효하지 않은 명령으로 생성 시 예외 발생")
    void createProduct_InvalidCommand_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
                .name("") // 빈 문자열
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품명은 필수입니다.");

        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("가격이 null인 명령으로 생성 시 예외 발생")
    void createProduct_NullPrice_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
                .name("테스트 상품")
                .price(null) // null 가격
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 필수입니다.");

        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("카테고리가 null인 명령으로 생성 시 예외 발생")
    void createProduct_NullCategory_ThrowsException() {
        // given
        ProductCreateCommand invalidCommand = ProductCreateCommand.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .category(null) // null 카테고리
                .build();

        // when & then
        assertThatThrownBy(() -> productCreateService.createProduct(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리는 필수입니다.");

        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }
}