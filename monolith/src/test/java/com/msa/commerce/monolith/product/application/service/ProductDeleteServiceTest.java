package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDeleteService 단위 테스트")
class ProductDeleteServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher productEventPublisher;

    @InjectMocks
    private ProductDeleteService productDeleteService;

    private Product testProduct;

    private Long productId;

    @BeforeEach
    void setUp() {
        productId = 1L;
        testProduct = Product.reconstitute(
            productId,
            "TEST-SKU-001",
            "Test Product",
            "Short description",
            "Detailed description",
            1L,
            "Test Brand",
            ProductType.PHYSICAL,
            ProductStatus.ACTIVE,
            new BigDecimal("10000"),
            new BigDecimal("8000"),
            "KRW",
            500,
            true,
            true,
            false,
            "test-product",
            "test,product",
            "http://example.com/image.jpg",
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1),
            null,
            1L
        );
    }

    @Test
    @DisplayName("정상적으로 상품을 삭제할 수 있다")
    void deleteProduct_Success() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // when
        productDeleteService.deleteProduct(productId);

        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
        verify(productEventPublisher).publishProductDeletedEvent(any(Product.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 ResourceNotFoundException 발생")
    void deleteProduct_NotFound() {
        // given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with id: " + productId)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND.getCode());

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 삭제된 상품 삭제 시 ValidationException 발생")
    void deleteProduct_AlreadyDeleted() {
        // given
        Product deletedProduct = Product.reconstitute(
            productId,
            "TEST-SKU-001",
            "Test Product",
            "Short description",
            "Detailed description",
            1L,
            "Test Brand",
            ProductType.PHYSICAL,
            ProductStatus.ARCHIVED,
            new BigDecimal("10000"),
            new BigDecimal("8000"),
            "KRW",
            500,
            true,
            true,
            false,
            "test-product",
            "test,product",
            "http://example.com/image.jpg",
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().minusHours(1), // 이미 삭제됨
            1L
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(deletedProduct));

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(productId))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Product is already deleted")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode());

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

}
