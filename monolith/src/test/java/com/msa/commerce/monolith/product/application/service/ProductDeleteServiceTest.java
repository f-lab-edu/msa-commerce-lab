package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
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
import org.springframework.context.ApplicationEventPublisher;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.monolith.product.adapter.out.persistence.ProductJpaEntity;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.service.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.domain.event.ProductEvent;
import com.msa.commerce.monolith.product.domain.service.InventoryDomainService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDeleteService 단위 테스트")
class ProductDeleteServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private InventoryDomainService inventoryDomainService;

    @InjectMocks
    private ProductDeleteService productDeleteService;

    private Product testProduct;

    private ProductJpaEntity testEntity;

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
            1,
            100,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1),
            null,
            1L
        );
        testEntity = ProductJpaEntity.fromDomainEntityForCreation(testProduct);
    }

    @Test
    @DisplayName("정상적으로 상품을 삭제할 수 있다")
    void deleteProduct_Success() {
        // given
        given(productRepository.findEntityById(eq(productId))).willReturn(Optional.of(testEntity));
        given(productMapper.entityToDomain(any(ProductJpaEntity.class))).willReturn(testProduct);
        doNothing().when(inventoryDomainService).disableInventoryForProduct(
            eq(productId), anyString(), anyString(), anyString());

        // when
        productDeleteService.deleteProduct(productId);

        // then
        verify(productRepository).findEntityById(eq(productId));
        verify(productMapper).entityToDomain(any(ProductJpaEntity.class));
        verify(applicationEventPublisher).publishEvent(any(ProductEvent.class));
        verify(inventoryDomainService).disableInventoryForProduct(
            eq(productId), anyString(), eq("PRODUCT_DELETION"), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외 발생")
    void deleteProduct_NotFound() {
        // given
        given(productRepository.findEntityById(eq(productId)))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with ID: " + productId);
    }

    @Test
    @DisplayName("이미 삭제된 상품 삭제 시 ValidationException 발생")
    void deleteProduct_AlreadyDeleted() {
        // given
        ProductJpaEntity deletedEntity = ProductJpaEntity.fromDomainEntityForCreation(testProduct);
        deletedEntity.softDelete(); // Mark as already deleted

        given(productRepository.findEntityById(eq(productId)))
            .willReturn(Optional.of(deletedEntity));

        // when & then
        assertThatThrownBy(() -> productDeleteService.deleteProduct(productId))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Product is already deleted")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode());
    }

    @Test
    @DisplayName("재고 비활성화 실패해도 상품 삭제는 성공한다")
    void deleteProduct_InventoryDisableFails_StillSucceeds() {
        // given
        given(productRepository.findEntityById(eq(productId))).willReturn(Optional.of(testEntity));
        given(productMapper.entityToDomain(any(ProductJpaEntity.class))).willReturn(testProduct);
        doThrow(new RuntimeException("Inventory service error"))
            .when(inventoryDomainService).disableInventoryForProduct(
                eq(productId), anyString(), anyString(), anyString());

        // when
        assertThatCode(() -> productDeleteService.deleteProduct(productId))
            .doesNotThrowAnyException();

        // then
        verify(productRepository).findEntityById(eq(productId));
        verify(productMapper).entityToDomain(any(ProductJpaEntity.class));
        verify(applicationEventPublisher).publishEvent(any(ProductEvent.class));
        verify(inventoryDomainService).disableInventoryForProduct(
            eq(productId), anyString(), eq("PRODUCT_DELETION"), anyString());
    }

}
