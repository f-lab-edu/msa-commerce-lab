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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.service.ProductCreateService;
import com.msa.commerce.monolith.product.domain.ProductCategory;
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
    private ProductMapper productMapper;

    @Mock
    private ProductSearchUseCase productSearchUseCase;

    @Mock
    private ProductCreateService productCreateService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductController(productCreateUseCase, productCreateService, productGetUseCase, productUpdateUseCase,
                    productSearchUseCase, productMapper))
            .setControllerAdvice(new com.msa.commerce.common.exception.GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("정상적인 상품 업데이트")
    void updateProduct_Success() throws Exception {
        // given
        Long productId = 1L;
        ProductUpdateCommand command = createUpdateCommand(productId);
        ProductResponse response = createProductResponse();

        given(productMapper.toUpdateCommand(eq(productId), any(ProductUpdateRequest.class))).willReturn(command);
        given(productUpdateUseCase.updateProduct(command)).willReturn(response);

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
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("업데이트된 상품명"))
            .andExpect(jsonPath("$.price").value(15000))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(productMapper).toUpdateCommand(eq(productId), any(ProductUpdateRequest.class));
        verify(productUpdateUseCase).updateProduct(command);
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

    private ProductUpdateRequest createUpdateRequest() {
        ProductUpdateRequest request = new ProductUpdateRequest();
        // Reflection을 사용하여 private 필드 설정하거나, 
        // 테스트용 생성자/빌더가 있다면 사용
        return request;
    }

    private ProductUpdateCommand createUpdateCommand(Long productId) {
        return ProductUpdateCommand.builder()
            .productId(productId)
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .price(new BigDecimal("15000"))
            .build();
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .categoryId(ProductCategory.ELECTRONICS.getId())
            .sku("TEST-SKU")
            .name("업데이트된 상품명")
            .description("업데이트된 설명")
            .price(new BigDecimal("15000"))
            .status(ProductStatus.ACTIVE)
            .visibility("PUBLIC")
            .isFeatured(false)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .build();
    }

}
