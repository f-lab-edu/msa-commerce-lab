package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

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

import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductInventoryRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.application.port.out.ProductViewCountPort;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductInventory;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 조회 서비스 테스트")
class ProductGetServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductInventoryRepository inventoryRepository;
    
    @Mock
    private ProductViewCountPort viewCountPort;
    
    @Mock
    private ProductResponseMapper responseMapper;
    
    @InjectMocks
    private ProductGetService productGetService;
    
    private Product activeProduct;
    private Product archivedProduct;
    private ProductInventory inventory;
    private ProductResponse productResponse;
    
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        activeProduct = Product.reconstitute(
            1L, 1L, "TEST-SKU-001", "Test Product", "Test Description",
            "Short desc", "Test Brand", "Test Model", new BigDecimal("100.00"),
            new BigDecimal("120.00"), new BigDecimal("80.00"), new BigDecimal("1.0"),
            "{}", ProductStatus.ACTIVE, "PUBLIC", "STANDARD",
            "Meta Title", "Meta Description", "test,product", true, now, now
        );
        
        archivedProduct = Product.reconstitute(
            2L, 1L, "TEST-SKU-002", "Archived Product", "Archived Description",
            "Archived short desc", "Test Brand", "Test Model", new BigDecimal("100.00"),
            new BigDecimal("120.00"), new BigDecimal("80.00"), new BigDecimal("1.0"),
            "{}", ProductStatus.ARCHIVED, "PUBLIC", "STANDARD",
            "Meta Title", "Meta Description", "test,archived", false, now, now
        );
        
        inventory = ProductInventory.create(1L, 100, 10, true, false,
            1, 999, 10, 50, "MAIN");
        
        productResponse = ProductResponse.builder()
            .id(1L)
            .name("Test Product")
            .price(new BigDecimal("100.00"))
            .status(ProductStatus.ACTIVE)
            .availableQuantity(100)
            .build();
    }
    
    @Test
    @DisplayName("정상적인 상품 조회가 성공해야 한다")
    void getProduct_Success() {
        // Given
        Long productId = 1L;
        given(productRepository.findById(productId)).willReturn(Optional.of(activeProduct));
        given(inventoryRepository.findByProductId(productId)).willReturn(Optional.of(inventory));
        given(responseMapper.toResponse(activeProduct, inventory)).willReturn(productResponse);
        
        // When
        ProductResponse result = productGetService.getProduct(productId);
        
        // Then
        assertThat(result).isEqualTo(productResponse);
        then(productRepository).should(times(1)).findById(productId);
        then(inventoryRepository).should(times(1)).findByProductId(productId);
        then(responseMapper).should(times(1)).toResponse(activeProduct, inventory);
        then(viewCountPort).should(times(1)).incrementViewCount(productId);
    }
    
    @Test
    @DisplayName("조회수 증가 없이 상품 조회가 성공해야 한다")
    void getProduct_WithoutViewCountIncrement_Success() {
        // Given
        Long productId = 1L;
        given(productRepository.findById(productId)).willReturn(Optional.of(activeProduct));
        given(inventoryRepository.findByProductId(productId)).willReturn(Optional.of(inventory));
        given(responseMapper.toResponse(activeProduct, inventory)).willReturn(productResponse);
        
        // When
        ProductResponse result = productGetService.getProduct(productId, false);
        
        // Then
        assertThat(result).isEqualTo(productResponse);
        then(viewCountPort).should(never()).incrementViewCount(any());
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외가 발생해야 한다")
    void getProduct_NotFound_ThrowsException() {
        // Given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> productGetService.getProduct(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product not found with id: " + productId);
        
        then(inventoryRepository).should(never()).findByProductId(any());
        then(responseMapper).should(never()).toResponse(any(), any());
        then(viewCountPort).should(never()).incrementViewCount(any());
    }
    
    @Test
    @DisplayName("삭제된(ARCHIVED) 상품 조회 시 예외가 발생해야 한다")
    void getProduct_ArchivedProduct_ThrowsException() {
        // Given
        Long productId = 2L;
        given(productRepository.findById(productId)).willReturn(Optional.of(archivedProduct));
        
        // When & Then
        assertThatThrownBy(() -> productGetService.getProduct(productId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Product not found with id: " + productId);
        
        then(inventoryRepository).should(never()).findByProductId(any());
        then(responseMapper).should(never()).toResponse(any(), any());
        then(viewCountPort).should(never()).incrementViewCount(any());
    }
    
    @Test
    @DisplayName("재고 정보가 없는 상품 조회가 성공해야 한다")
    void getProduct_WithoutInventory_Success() {
        // Given
        Long productId = 1L;
        given(productRepository.findById(productId)).willReturn(Optional.of(activeProduct));
        given(inventoryRepository.findByProductId(productId)).willReturn(Optional.empty());
        given(responseMapper.toResponse(activeProduct, null)).willReturn(productResponse);
        
        // When
        ProductResponse result = productGetService.getProduct(productId);
        
        // Then
        assertThat(result).isEqualTo(productResponse);
        then(responseMapper).should(times(1)).toResponse(activeProduct, null);
        then(viewCountPort).should(times(1)).incrementViewCount(productId);
    }
}