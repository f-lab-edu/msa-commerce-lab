package com.msa.commerce.monolith.product.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.common.exception.ValidationException;
import com.msa.commerce.common.monitoring.MetricsCollector;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductDeleteUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController 삭제 API 단위 테스트")
class ProductControllerDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCreateUseCase productCreateUseCase;

    @MockitoBean
    private ProductGetUseCase productGetUseCase;

    @MockitoBean
    private ProductUpdateUseCase productUpdateUseCase;

    @MockitoBean
    private ProductDeleteUseCase productDeleteUseCase;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private MetricsCollector metricsCollector;

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - 정상적으로 상품 삭제")
    @WithMockUser
    void deleteProduct_Success() throws Exception {
        // given
        Long productId = 1L;
        doNothing().when(productDeleteUseCase).deleteProduct(productId);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(productDeleteUseCase, times(1)).deleteProduct(eq(productId));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - 존재하지 않는 상품 삭제 시 404 반환")
    @WithMockUser
    void deleteProduct_NotFound() throws Exception {
        // given
        Long productId = 999L;
        doThrow(new ResourceNotFoundException("Product not found", ErrorCode.PRODUCT_NOT_FOUND.getCode()))
            .when(productDeleteUseCase).deleteProduct(productId);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isNotFound());

        verify(productDeleteUseCase, times(1)).deleteProduct(eq(productId));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - 진행 중인 주문이 있는 상품 삭제 시 400 반환")
    @WithMockUser
    void deleteProduct_WithActiveOrders() throws Exception {
        // given
        Long productId = 1L;
        doThrow(new ValidationException("Cannot delete product with active orders",
            ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode()))
            .when(productDeleteUseCase).deleteProduct(productId);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isBadRequest());

        verify(productDeleteUseCase, times(1)).deleteProduct(eq(productId));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - 이미 삭제된 상품 삭제 시 400 반환")
    @WithMockUser
    void deleteProduct_AlreadyDeleted() throws Exception {
        // given
        Long productId = 1L;
        doThrow(new ValidationException("Product is already deleted",
            ErrorCode.PRODUCT_UPDATE_NOT_ALLOWED.getCode()))
            .when(productDeleteUseCase).deleteProduct(productId);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isBadRequest());

        verify(productDeleteUseCase, times(1)).deleteProduct(eq(productId));
    }

}
