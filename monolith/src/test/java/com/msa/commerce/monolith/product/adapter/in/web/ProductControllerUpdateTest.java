package com.msa.commerce.monolith.product.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.adapter.in.web.mapper.ProductMapper;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductDeleteUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyUseCase;
import com.msa.commerce.monolith.product.application.port.in.command.ProductUpdateCommand;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController 업데이트 API 테스트")
class ProductControllerUpdateTest {

    private MockMvc mockMvc;

    @Mock
    private ProductCreateUseCase productCreateUseCase;

    @Mock
    private ProductGetUseCase productGetUseCase;

    @Mock
    private ProductUpdateUseCase productUpdateUseCase;

    @Mock
    private ProductDeleteUseCase productDeleteUseCase;

    @Mock
    private ProductVerifyUseCase productVerifyUseCase;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductController(productCreateUseCase, productGetUseCase, productUpdateUseCase, productDeleteUseCase, productVerifyUseCase, productMapper))
            .setControllerAdvice(new com.msa.commerce.common.exception.GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("정상적인 상품 업데이트")
    @WithMockUser
    void updateProduct_Success() throws Exception {
        // Given
        Long productId = 1L;
        String requestJson = createUpdateRequestJson();
        ProductUpdateCommand command = createMockUpdateCommand(productId);
        ProductResponse response = createMockUpdatedProductResponse();

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command)).willReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.id").value(1L),
                jsonPath("$.name").value("업데이트된 상품명"),
                jsonPath("$.description").value("업데이트된 설명"),
                jsonPath("$.basePrice").value(15000),
                jsonPath("$.status").value("ACTIVE")
            );

        verify(productMapper).toUpdateCommand(eq(productId), any(ProductUpdateRequest.class));
        verify(productUpdateUseCase).updateProduct(command);
    }

    private String createUpdateRequestJson() {
        return """
            {
                "name": "업데이트된 상품명",
                "description": "업데이트된 설명",
                "price": 15000
            }
            """;
    }

    @Test
    @DisplayName("필드가 없는 요청으로 업데이트 시 400 에러")
    void updateProduct_NoFields_ReturnsBadRequest() throws Exception {
        // given
        Long productId = 1L;

        // mapper가 IllegalArgumentException을 던짐
        when(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class)))
            .thenThrow(new IllegalArgumentException("No fields to update provided."));

        String emptyRequestJson = "{}";

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyRequestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효하지 않은 요청으로 업데이트 시 400 에러")
    void updateProduct_InvalidRequest_ReturnsBadRequest() throws Exception {
        // given
        Long productId = 1L;
        ProductUpdateCommand command = createUpdateCommand(productId);

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command))
            .willThrow(new IllegalArgumentException("Invalid input"));

        String requestJson = """
            {
                "name": "업데이트된 상품명",
                "description": "업데이트된 설명",
                "price": 15000
            }
            """;

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());

        verify(productUpdateUseCase).updateProduct(command);
    }

    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 404 에러")
    void updateProduct_ProductNotFound_ReturnsNotFound() throws Exception {
        // given
        Long productId = 999L;
        ProductUpdateCommand command = createUpdateCommand(productId);

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command))
            .willThrow(
                new ResourceNotFoundException("Product not found with ID: 999", ErrorCode.PRODUCT_NOT_FOUND.getCode()));

        String requestJson = """
            {
                "name": "업데이트된 상품명",
                "description": "업데이트된 설명",
                "price": 15000
            }
            """;

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isNotFound());

        verify(productUpdateUseCase).updateProduct(command);
    }

    @Test
    @DisplayName("서버 에러 발생 시 500 에러")
    void updateProduct_ServerError_ReturnsInternalServerError() throws Exception {
        // given
        Long productId = 1L;
        ProductUpdateCommand command = createUpdateCommand(productId);

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command))
            .willThrow(new RuntimeException("Database connection error"));

        String requestJson = """
            {
                "name": "업데이트된 상품명",
                "description": "업데이트된 설명",
                "price": 15000
            }
            """;

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isInternalServerError());

        verify(productUpdateUseCase).updateProduct(command);
    }

    @Test
    @DisplayName("부분 업데이트 - 이름만 변경")
    void updateProduct_PartialUpdate_NameOnly() throws Exception {
        // given
        Long productId = 1L;
        String requestJson = "{ \"name\": \"새로운 상품명\" }";

        ProductUpdateCommand command = ProductUpdateCommand.builder()
            .productId(productId)
            .name("새로운 상품명")
            .build();

        ProductResponse response = createProductResponse();

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command)).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("업데이트된 상품명"));

        verify(productUpdateUseCase).updateProduct(any(ProductUpdateCommand.class));
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 요청 시 400 에러")
    void updateProduct_InvalidJson_ReturnsBadRequest() throws Exception {
        // given
        Long productId = 1L;
        String invalidJson = "{ \"name\": \"상품명\", \"price\": invalid }";

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효성 검증 실패 시 400 에러")
    void updateProduct_ValidationFailed_ReturnsBadRequest() throws Exception {
        // given
        Long productId = 1L;
        String requestJson = "{ \"price\": -100 }"; // 음수 가격

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    private ProductUpdateCommand createUpdateCommand(Long productId) {
        return ProductUpdateCommand.builder()
            .productId(productId)
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .basePrice(new BigDecimal("15000"))
            .build();
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .categoryId(1L)
            .sku("TEST-SKU")
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .basePrice(new BigDecimal("15000"))
            .status(ProductStatus.ACTIVE)
            .isFeatured(false)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private ProductUpdateCommand createMockUpdateCommand(Long productId) {
        return ProductUpdateCommand.builder()
            .productId(productId)
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .basePrice(new BigDecimal("15000"))
            .build();
    }

    private ProductResponse createMockUpdatedProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .sku("TEST-SKU-001")
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .basePrice(new BigDecimal("15000"))
            .categoryId(1L)
            .status(ProductStatus.ACTIVE)
            .build();
    }

}
