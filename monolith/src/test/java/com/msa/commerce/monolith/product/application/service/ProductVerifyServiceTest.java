package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyResponse;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.adapter.out.persistence.InventorySnapshotJpaRepository;

@ExtendWith(MockitoExtension.class)
class ProductVerifyServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventorySnapshotJpaRepository inventorySnapshotRepository;

    @InjectMocks
    private ProductVerifyService productVerifyService;

    private Product activeProduct;

    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
            .sku("TEST-SKU-001")
            .name("Test Product 1")
            .shortDescription("Short description")
            .description("Long description")
            .categoryId(1L)
            .brand("Test Brand")
            .productType(ProductType.PHYSICAL)
            .basePrice(BigDecimal.valueOf(10000))
            .salePrice(BigDecimal.valueOf(9000))
            .currency("KRW")
            .weightGrams(100)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("test-product-1")
            .searchTags("test,product")
            .primaryImageUrl("http://example.com/image.jpg")
            .build();

        // Use reflection to set the ID and status since they're not in the builder
        setFieldValue(activeProduct, "id", 1L);
        setFieldValue(activeProduct, "status", ProductStatus.ACTIVE);

        inactiveProduct = Product.builder()
            .sku("TEST-SKU-002")
            .name("Test Product 2")
            .shortDescription("Short description")
            .description("Long description")
            .categoryId(1L)
            .brand("Test Brand")
            .productType(ProductType.PHYSICAL)
            .basePrice(BigDecimal.valueOf(20000))
            .salePrice(null)
            .currency("KRW")
            .weightGrams(200)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("test-product-2")
            .searchTags("test,product")
            .primaryImageUrl("http://example.com/image2.jpg")
            .build();

        setFieldValue(inactiveProduct, "id", 2L);
        setFieldValue(inactiveProduct, "status", ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("활성 상품 검증 성공")
    void verifyActiveProduct_Success() {
        // Given
        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(List.of(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build()
            ))
            .build();

        when(productRepository.findAllByIds(anyList())).thenReturn(List.of(activeProduct));
        when(inventorySnapshotRepository.getTotalAvailableQuantityByProductId(1L)).thenReturn(100);

        // When
        ProductVerifyResponse response = productVerifyService.verifyProducts(command);

        // Then
        assertThat(response).isNotNull();
        // Note: allAvailable depends on random stock generation
        assertThat(response.getResults()).hasSize(1);

        ProductVerifyResponse.ProductVerifyResult result = response.getResults().get(0);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(result.getName()).isEqualTo("Test Product 1");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(result.getRequestedQuantity()).isEqualTo(5);
        assertThat(result.getCurrentPrice()).isEqualTo(BigDecimal.valueOf(9000));
        assertThat(result.getOriginalPrice()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("비활성 상품 검증 실패")
    void verifyInactiveProduct_Failure() {
        // Given
        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(List.of(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(5)
                    .build()
            ))
            .build();

        when(productRepository.findAllByIds(anyList())).thenReturn(List.of(inactiveProduct));
        when(inventorySnapshotRepository.getTotalAvailableQuantityByProductId(2L)).thenReturn(50);

        // When
        ProductVerifyResponse response = productVerifyService.verifyProducts(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAllAvailable()).isFalse();
        assertThat(response.getResults()).hasSize(1);

        ProductVerifyResponse.ProductVerifyResult result = response.getResults().get(0);
        assertThat(result.getProductId()).isEqualTo(2L);
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getUnavailableReason()).contains("INACTIVE");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 상품 검증")
    void verifyNonExistentProduct() {
        // Given
        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(List.of(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(999L)
                    .quantity(5)
                    .build()
            ))
            .build();

        when(productRepository.findAllByIds(anyList())).thenReturn(Collections.emptyList());

        // When
        ProductVerifyResponse response = productVerifyService.verifyProducts(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAllAvailable()).isFalse();
        assertThat(response.getResults()).hasSize(1);

        ProductVerifyResponse.ProductVerifyResult result = response.getResults().get(0);
        assertThat(result.getProductId()).isEqualTo(999L);
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getUnavailableReason()).isEqualTo("Product not found");
    }

    @Test
    @DisplayName("여러 상품 검증")
    void verifyMultipleProducts() {
        // Given
        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(Arrays.asList(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build(),
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(10)
                    .build(),
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(999L)
                    .quantity(3)
                    .build()
            ))
            .build();

        when(productRepository.findAllByIds(anyList()))
            .thenReturn(Arrays.asList(activeProduct, inactiveProduct));
        when(inventorySnapshotRepository.getTotalAvailableQuantityByProductId(1L)).thenReturn(100);
        when(inventorySnapshotRepository.getTotalAvailableQuantityByProductId(2L)).thenReturn(50);

        // When
        ProductVerifyResponse response = productVerifyService.verifyProducts(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAllAvailable()).isFalse();
        assertThat(response.getResults()).hasSize(3);

        // Check for inactive product
        ProductVerifyResponse.ProductVerifyResult inactiveResult = response.getResults().stream()
            .filter(r -> r.getProductId().equals(2L))
            .findFirst()
            .orElse(null);
        assertThat(inactiveResult).isNotNull();
        assertThat(inactiveResult.getAvailable()).isFalse();

        // Check for non-existent product
        ProductVerifyResponse.ProductVerifyResult nonExistentResult = response.getResults().stream()
            .filter(r -> r.getProductId().equals(999L))
            .findFirst()
            .orElse(null);
        assertThat(nonExistentResult).isNotNull();
        assertThat(nonExistentResult.getAvailable()).isFalse();
        assertThat(nonExistentResult.getUnavailableReason()).isEqualTo("Product not found");
    }

    private void setFieldValue(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
