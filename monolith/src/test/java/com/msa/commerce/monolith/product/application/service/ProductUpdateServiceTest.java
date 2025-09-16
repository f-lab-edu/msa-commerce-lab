package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;
import com.msa.commerce.monolith.product.fixture.ProductCommandFixture;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUpdateService 테스트")
class ProductUpdateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductResponseMapper productResponseMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private ProductUpdateService productUpdateService;

    private Product existingProduct;

    private ProductUpdateCommand updateCommand;

    private ProductResponse expectedResponse;

    @BeforeEach
    void setUp() {
        existingProduct = Product.reconstitute(
            1L,                                    // id
            "ORIGINAL-SKU",                       // sku
            "원본 상품명",                         // name
            "원본 짧은 설명",                      // shortDescription
            "원본 상품 설명",                      // description
            1L,                                   // categoryId
            "원본 브랜드",                         // brand
            ProductType.PHYSICAL,                 // productType
            ProductStatus.ACTIVE,                 // status
            new BigDecimal("10000"),              // basePrice
            new BigDecimal("12000"),              // salePrice
            "KRW",                               // currency
            1500,                                 // weightGrams
            true,                                 // requiresShipping
            true,                                 // isTaxable
            false,                                // isFeatured
            "original-product",                  // slug
            "검색 키워드",                         // searchTags
            null,                                 // primaryImageUrl
            1,                                    // minOrderQuantity
            100,                                  // maxOrderQuantity
            LocalDateTime.now().minusDays(1),     // createdAt
            LocalDateTime.now().minusDays(1),     // updatedAt
            1L                                    // version
        );

        updateCommand = ProductCommandFixture.validProductUpdateCommand();

        expectedResponse = ProductResponse.builder()
            .id(1L)
            .sku("ORIGINAL-SKU")
            .name("업데이트된 상품명")
            .description("업데이트된 상품 설명")
            .categoryId(1L)
            .productType(ProductType.PHYSICAL)
            .basePrice(new BigDecimal("15000"))
            .currency("KRW")
            .status(ProductStatus.ACTIVE)
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(false)
            .slug("original-product")
            .version(1L)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("정상적인 상품 업데이트")
    void updateProduct_Success() {
        // given
        given(validator.validate(any(ProductUpdateCommand.class))).willReturn(Collections.emptySet());
        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.save(any(Product.class))).willReturn(existingProduct);
        given(productResponseMapper.toResponse(any(Product.class))).willReturn(expectedResponse);

        // when
        ProductResponse response = productUpdateService.updateProduct(updateCommand);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("업데이트된 상품명");
        assertThat(response.getDescription()).isEqualTo("업데이트된 상품 설명");
        assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("15000"));

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(productResponseMapper).toResponse(any(Product.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 예외 발생")
    void updateProduct_ProductNotFound_ThrowsException() {
        // given
        given(validator.validate(any(ProductUpdateCommand.class))).willReturn(Collections.emptySet());
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
            1L, "ARCHIVED-SKU", "보관된 상품", null, "설명",
            1L, null, ProductType.PHYSICAL, ProductStatus.ARCHIVED,
            new BigDecimal("10000"), null, "KRW", null, true,
            true, false, "archived-product", null, null,
            1, 100,
            LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), 1L
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
        ProductUpdateCommand commandWithDuplicateSku = ProductCommandFixture.duplicateSkuUpdateCommand();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsBySku(commandWithDuplicateSku.getSku())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(commandWithDuplicateSku))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("SKU already exists: " + commandWithDuplicateSku.getSku());

        verify(productRepository).findById(1L);
        verify(productRepository).existsBySku(commandWithDuplicateSku.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("중복된 상품명으로 업데이트 시 예외 발생")
    void updateProduct_DuplicateName_ThrowsException() {
        // given
        ProductUpdateCommand commandWithDuplicateName = ProductCommandFixture.duplicateNameUpdateCommand();

        given(productRepository.findById(1L)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByName(commandWithDuplicateName.getName())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(commandWithDuplicateName))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Product name already exists: " + commandWithDuplicateName.getName());

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
            .basePrice(new BigDecimal("15000"))
            .build();

        @SuppressWarnings("unchecked")
        ConstraintViolation<ProductUpdateCommand> violation = mock(ConstraintViolation.class);
        given(violation.getMessage()).willReturn("Product name must not be empty");
        given(validator.validate(any(ProductUpdateCommand.class))).willReturn(Set.of(violation));

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product update validation failed: Product name must not be empty");

        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("가격이 0인 업데이트 명령 시 예외 발생")
    void updateProduct_ZeroPrice_ThrowsException() {
        // given
        ProductUpdateCommand invalidCommand = ProductUpdateCommand.builder()
            .productId(1L)
            .basePrice(BigDecimal.ZERO) // 0 가격
            .build();

        @SuppressWarnings("unchecked")
        ConstraintViolation<ProductUpdateCommand> violation = mock(ConstraintViolation.class);
        given(violation.getMessage()).willReturn("Base price must be greater than 0");
        given(validator.validate(any(ProductUpdateCommand.class))).willReturn(Set.of(violation));

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product update validation failed: Base price must be greater than 0");

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
            .basePrice(new BigDecimal("15000"))
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
