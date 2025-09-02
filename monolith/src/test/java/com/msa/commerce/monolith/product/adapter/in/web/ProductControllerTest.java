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

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController 테스트")
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductCreateUseCase productCreateUseCase;

    @Mock
    private ProductGetUseCase productGetUseCase;

    @Mock
    private ProductUpdateUseCase productUpdateUseCase;

    @Mock
    private ProductSearchUseCase productSearchUseCase;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductController(productCreateUseCase, productGetUseCase, productUpdateUseCase,
                    productMapper))
            .setControllerAdvice(new com.msa.commerce.common.exception.GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("정상적인 상품 생성 요청")
    @WithMockUser
    void createProduct_Success() throws Exception {
        // Given
        String requestJson = createProductRequestJson();
        ProductResponse response = createMockProductResponse();

        given(productCreateUseCase.createProduct(any())).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.id").value(1L),
                jsonPath("$.sku").value("TEST-SKU-001"),
                jsonPath("$.name").value("테스트 상품"),
                jsonPath("$.description").value("테스트 상품 설명"),
                jsonPath("$.basePrice").value(10000),
                jsonPath("$.categoryId").value(1),
                jsonPath("$.status").value("DRAFT")
            );
    }

    @Test
    @DisplayName("상품명이 없는 요청 - 400 Bad Request")
    void createProduct_EmptyName_BadRequest() throws Exception {
        // given
        String requestJson = """
            {
                "description": "테스트 상품 설명",
                "price": 10000,
                "categoryId": 1
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("가격이 음수인 요청 - 400 Bad Request")
    void createProduct_NegativePrice_BadRequest() throws Exception {
        // given
        String requestJson = """
            {
                "sku": "TEST-SKU-002",
                "name": "테스트 상품",
                "description": "테스트 상품 설명",
                "price": -1000,
                "categoryId": 1
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 ID가 유효하지 않은 요청 - 400 Bad Request")
    void createProduct_InvalidCategoryId_BadRequest() throws Exception {
        // given
        String requestJson = """
            {
                "sku": "TEST-SKU-003",
                "name": "테스트 상품",
                "description": "테스트 상품 설명",
                "price": 10000,
                "categoryId": 999
            }
            """;

        given(productCreateUseCase.createProduct(any()))
            .willThrow(new IllegalArgumentException("Invalid category ID: 999"));

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 ID가 없는 요청 - 400 Bad Request")
    void createProduct_NoCategoryId_BadRequest() throws Exception {
        // given
        String requestJson = """
            {
                "sku": "TEST-SKU-004",
                "name": "테스트 상품",
                "description": "테스트 상품 설명",
                "price": 10000
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복된 SKU로 생성 시 예외 처리")
    void createProduct_DuplicateSku_ThrowsException() throws Exception {
        // given
        String requestJson = """
            {
                "sku": "TEST-SKU-005",
                "name": "테스트 상품",
                "description": "테스트 상품 설명",
                "price": 10000,
                "categoryId": 1
            }
            """;

        given(productCreateUseCase.createProduct(any()))
            .willThrow(new DuplicateResourceException(
                "Product SKU already exists: TEST-12345678",
                ErrorCode.PRODUCT_SKU_DUPLICATE.getCode()
            ));

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Product SKU already exists: TEST-12345678"))
            .andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_SKU_DUPLICATE.getCode()))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/v1/products"));
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .categoryId(1L)
            .sku("TEST1234")
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .basePrice(new BigDecimal("10000"))
            .status(ProductStatus.DRAFT)
            .isFeatured(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private String createProductRequestJson() {
        return """
            {
                "sku": "TEST-SKU-001",
                "name": "테스트 상품",
                "description": "테스트 상품 설명",
                "price": 10000,
                "categoryId": 1
            }
            """;
    }

    private ProductResponse createMockProductResponse() {
        return ProductResponse.builder()
            .id(1L)
            .sku("TEST-SKU-001")
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .basePrice(new BigDecimal("10000"))
            .categoryId(1L)
            .status(ProductStatus.DRAFT)
            .build();
    }

}
