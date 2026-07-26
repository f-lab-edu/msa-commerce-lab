package com.msa.commerce.monolith.product.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.monolith.product.application.port.out.ProductCategoryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCategoryValidator 단위 테스트")
class ProductCategoryValidatorTest {

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private ProductCategoryValidator productCategoryValidator;

    @Test
    @DisplayName("활성 카테고리는 검증을 통과한다")
    void passesForActiveCategory() {
        when(productCategoryRepository.existsActiveById(1L)).thenReturn(true);

        assertThatCode(() -> productCategoryValidator.validateActiveCategory(1L))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않거나 비활성인 카테고리는 거부된다")
    void rejectsMissingOrInactiveCategory() {
        when(productCategoryRepository.existsActiveById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productCategoryValidator.validateActiveCategory(99L))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("categoryId=99");
    }

    @Test
    @DisplayName("categoryId가 null이면 검증을 건너뛴다")
    void skipsNullCategoryId() {
        productCategoryValidator.validateActiveCategory(null);

        verifyNoInteractions(productCategoryRepository);
    }

}
