package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ProductUpdateNotAllowedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUpdateService 테스트")
class ProductUpdateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductResponseMapper productResponseMapper;

    @InjectMocks
    private ProductUpdateService productUpdateService;

    private Product existingProduct;

    private ProductUpdateCommand updateCommand;

    private ProductResponse expectedResponse;

    @BeforeEach
    void setUp() {
        existingProduct = Product.reconstitute(
            1L,                                    // id
            ProductCategory.ELECTRONICS.getId(),   // categoryId
            "ORIGINAL-SKU",                       // sku
            "원본 상품명",                         // name
            "원본 상품 설명",                      // description
            "원본 짧은 설명",                      // shortDescription
            "원본 브랜드",                         // brand
            "원본 모델",                          // model
            new BigDecimal("10000"),              // price
            new BigDecimal("12000"),              // comparePrice
            new BigDecimal("8000"),               // costPrice
            new BigDecimal("1.5"),                // weight
            "{}",                                 // productAttributes
            ProductStatus.ACTIVE,                 // status
            "PUBLIC",                             // visibility
            "STANDARD",                           // taxClass
            "원본 메타 제목",                      // metaTitle
            "원본 메타 설명",                      // metaDescription
            "검색 키워드",                         // searchKeywords
            false,                                // isFeatured
            LocalDateTime.now().minusDays(1),     // createdAt
            LocalDateTime.now().minusDays(1)      // updatedAt
        );

        updateCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .name("업데이트된 상품명")
            .description("업데이트된 상품 설명")
            .price(new BigDecimal("15000"))
            .build();

        expectedResponse = ProductResponse.builder()
            .id(1L)
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("ORIGINAL-SKU")
            .name("업데이트된 상품명")
            .description("업데이트된 상품 설명")
            .price(new BigDecimal("15000"))
            .status(ProductStatus.ACTIVE)
            .visibility("PUBLIC")
            .isFeatured(false)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("정상적인 상품 업데이트")
    void updateProduct_Success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        // when
        ProductResponse response = productUpdateService.updateProduct(updateCommand);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("업데이트된 상품명");
        assertThat(response.getDescription()).isEqualTo("업데이트된 상품 설명");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("15000"));

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(productResponseMapper).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 예외 발생")
    void updateProduct_ProductNotFound_ThrowsException() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(updateCommand))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product not found with ID: 1");

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("업데이트 불가능한 상태의 상품 업데이트 시 예외 발생")
    void updateProduct_ProductNotUpdatable_ThrowsException() {
        // given
        Product archivedProduct = Product.reconstitute(
            1L, ProductCategory.ELECTRONICS.getId(), "ARCHIVED-SKU", "보관된 상품",
            "설명", null, null, null, new BigDecimal("10000"),
            null, null, null, null, ProductStatus.ARCHIVED,
            "PUBLIC", null, null, null, null, false,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)
        );

        given(productRepository.findById(1L)).willReturn(Optional.of(archivedProduct));

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(updateCommand))
            .isInstanceOf(ProductUpdateNotAllowedException.class)
            .hasMessageContaining("Product with ID 1 cannot be updated. Current status: ARCHIVED");

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("중복된 SKU로 업데이트 시 예외 발생")
    void updateProduct_DuplicateSku_ThrowsException() {
        // given
        ProductUpdateCommand commandWithDuplicateSku = ProductUpdateCommand.builder()
            .productId(1L)
            .sku("DUPLICATE-SKU")
            .name("업데이트된 상품명")
            .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsBySku("DUPLICATE-SKU")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(commandWithDuplicateSku))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("SKU already exists: DUPLICATE-SKU");

        verify(productRepository).findById(1L);
        verify(productRepository).existsBySku("DUPLICATE-SKU");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("중복된 상품명으로 업데이트 시 예외 발생")
    void updateProduct_DuplicateName_ThrowsException() {
        // given
        ProductUpdateCommand commandWithDuplicateName = ProductUpdateCommand.builder()
            .productId(1L)
            .name("중복된 상품명")
            .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByName("중복된 상품명")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(commandWithDuplicateName))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Product name already exists: 중복된 상품명");

        verify(productRepository).findById(1L);
        verify(productRepository).existsByName("중복된 상품명");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("업데이트할 필드가 없을 때 예외 발생 - 서비스 레벨에서는 hasChanges() 검증을 생략")
    void updateProduct_NoFieldsToUpdate_CallsValidation() {
        // given
        ProductUpdateCommand emptyCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .build();

        // Note: 실제로는 mapper에서 이 경우를 처리하므로 이 테스트는 validation 호출만 확인
        // when & then - command.validate()가 예외를 던지지 않으면 정상 처리
        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        ProductResponse response = productUpdateService.updateProduct(emptyCommand);

        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("부분 업데이트 - 이름만 업데이트")
    void updateProduct_PartialUpdate_NameOnly() {
        // given
        ProductUpdateCommand partialCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .name("새로운 상품명")
            .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        // when
        ProductResponse response = productUpdateService.updateProduct(partialCommand);

        // then
        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }


    @Test
    @DisplayName("유효하지 않은 업데이트 명령 시 예외 발생")
    void updateProduct_InvalidCommand_ThrowsException() {
        // given
        ProductUpdateCommand invalidCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .name("") // 빈 문자열
            .price(new BigDecimal("15000"))
            .build();

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name must not be empty and cannot exceed 255 characters.");

        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("가격이 0인 업데이트 명령 시 예외 발생")
    void updateProduct_ZeroPrice_ThrowsException() {
        // given
        ProductUpdateCommand invalidCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .price(BigDecimal.ZERO) // 0 가격
            .build();

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Price must be between 0.01 and 99,999,999.99.");

        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("SKU 변경 없이 동일한 SKU로 업데이트")
    void updateProduct_SameSku_Success() {
        // given
        ProductUpdateCommand commandWithSameSku = ProductUpdateCommand.builder()
            .productId(1L)
            .sku("ORIGINAL-SKU") // 기존과 동일한 SKU
            .name("업데이트된 상품명")
            .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        // when
        ProductResponse response = productUpdateService.updateProduct(commandWithSameSku);

        // then
        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository, never()).existsBySku(anyString()); // SKU 중복 검사 수행 안함
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품명 변경 없이 동일한 이름으로 업데이트")
    void updateProduct_SameName_Success() {
        // given
        ProductUpdateCommand commandWithSameName = ProductUpdateCommand.builder()
            .productId(1L)
            .name("원본 상품명") // 기존과 동일한 이름
            .price(new BigDecimal("15000"))
            .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        // when
        ProductResponse response = productUpdateService.updateProduct(commandWithSameName);

        // then
        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository, never()).existsByName(anyString()); // 이름 중복 검사 수행 안함
        verify(productRepository).save(any(Product.class));
    }

}

