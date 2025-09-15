package com.msa.commerce.monolith.product.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyUseCase;
import com.msa.commerce.monolith.product.adapter.in.web.mapper.ProductMapper;
import com.msa.commerce.monolith.product.domain.ProductStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController 검증 API 테스트")
class ProductControllerVerifyTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ProductCreateUseCase productCreateUseCase;

    @Mock
    private ProductGetUseCase productGetUseCase;

    @Mock
    private ProductUpdateUseCase productUpdateUseCase;

    @Mock
    private ProductVerifyUseCase productVerifyUseCase;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductController(productCreateUseCase, productGetUseCase, productUpdateUseCase,
                    productVerifyUseCase, productMapper))
            .setControllerAdvice(new com.msa.commerce.common.exception.GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("상품 검증 API - 모든 상품 검증 성공")
    void verifyProducts_AllAvailable() throws Exception {
        // Given
        ProductVerifyRequest request = ProductVerifyRequest.builder()
            .items(Arrays.asList(
                ProductVerifyRequest.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build(),
                ProductVerifyRequest.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(10)
                    .build()
            ))
            .build();

        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(Arrays.asList(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build(),
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(10)
                    .build()
            ))
            .build();

        ProductVerifyResponse response = ProductVerifyResponse.builder()
            .allAvailable(true)
            .results(Arrays.asList(
                ProductVerifyResponse.ProductVerifyResult.builder()
                    .productId(1L)
                    .sku("SKU-001")
                    .name("Product 1")
                    .available(true)
                    .status(ProductStatus.ACTIVE)
                    .requestedQuantity(5)
                    .availableStock(100)
                    .currentPrice(BigDecimal.valueOf(10000))
                    .originalPrice(BigDecimal.valueOf(10000))
                    .minOrderQuantity(1)
                    .maxOrderQuantity(100)
                    .build(),
                ProductVerifyResponse.ProductVerifyResult.builder()
                    .productId(2L)
                    .sku("SKU-002")
                    .name("Product 2")
                    .available(true)
                    .status(ProductStatus.ACTIVE)
                    .requestedQuantity(10)
                    .availableStock(50)
                    .currentPrice(BigDecimal.valueOf(20000))
                    .originalPrice(BigDecimal.valueOf(20000))
                    .minOrderQuantity(1)
                    .maxOrderQuantity(100)
                    .build()
            ))
            .build();

        when(productMapper.toVerifyCommand(any(ProductVerifyRequest.class))).thenReturn(command);
        when(productVerifyUseCase.verifyProducts(any(ProductVerifyCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/products/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allAvailable").value(true))
            .andExpect(jsonPath("$.results").isArray())
            .andExpect(jsonPath("$.results[0].productId").value(1))
            .andExpect(jsonPath("$.results[0].available").value(true))
            .andExpect(jsonPath("$.results[0].status").value("ACTIVE"))
            .andExpect(jsonPath("$.results[1].productId").value(2))
            .andExpect(jsonPath("$.results[1].available").value(true));
    }

    @Test
    @DisplayName("상품 검증 API - 일부 상품 검증 실패")
    void verifyProducts_SomeUnavailable() throws Exception {
        // Given
        ProductVerifyRequest request = ProductVerifyRequest.builder()
            .items(Arrays.asList(
                ProductVerifyRequest.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build(),
                ProductVerifyRequest.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(100)
                    .build()
            ))
            .build();

        ProductVerifyCommand command = ProductVerifyCommand.builder()
            .items(Arrays.asList(
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(1L)
                    .quantity(5)
                    .build(),
                ProductVerifyCommand.ProductVerifyItem.builder()
                    .productId(2L)
                    .quantity(100)
                    .build()
            ))
            .build();

        ProductVerifyResponse response = ProductVerifyResponse.builder()
            .allAvailable(false)
            .results(Arrays.asList(
                ProductVerifyResponse.ProductVerifyResult.builder()
                    .productId(1L)
                    .sku("SKU-001")
                    .name("Product 1")
                    .available(true)
                    .status(ProductStatus.ACTIVE)
                    .requestedQuantity(5)
                    .availableStock(100)
                    .currentPrice(BigDecimal.valueOf(10000))
                    .originalPrice(BigDecimal.valueOf(10000))
                    .minOrderQuantity(1)
                    .maxOrderQuantity(100)
                    .build(),
                ProductVerifyResponse.ProductVerifyResult.builder()
                    .productId(2L)
                    .sku("SKU-002")
                    .name("Product 2")
                    .available(false)
                    .status(ProductStatus.ACTIVE)
                    .requestedQuantity(100)
                    .availableStock(50)
                    .currentPrice(BigDecimal.valueOf(20000))
                    .originalPrice(BigDecimal.valueOf(20000))
                    .unavailableReason("Insufficient stock")
                    .minOrderQuantity(1)
                    .maxOrderQuantity(100)
                    .build()
            ))
            .build();

        when(productMapper.toVerifyCommand(any(ProductVerifyRequest.class))).thenReturn(command);
        when(productVerifyUseCase.verifyProducts(any(ProductVerifyCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/products/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allAvailable").value(false))
            .andExpect(jsonPath("$.results[0].available").value(true))
            .andExpect(jsonPath("$.results[1].available").value(false))
            .andExpect(jsonPath("$.results[1].unavailableReason").value("Insufficient stock"));
    }

    @Test
    @DisplayName("상품 검증 API - 빈 요청 검증 실패")
    void verifyProducts_EmptyRequest() throws Exception {
        // Given
        ProductVerifyRequest request = ProductVerifyRequest.builder()
            .items(List.of())
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/products/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 검증 API - 잘못된 상품 ID")
    void verifyProducts_InvalidProductId() throws Exception {
        // Given
        String invalidRequest = """
            {
                "items": [
                    {
                        "productId": -1,
                        "quantity": 5
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 검증 API - 잘못된 수량")
    void verifyProducts_InvalidQuantity() throws Exception {
        // Given
        String invalidRequest = """
            {
                "items": [
                    {
                        "productId": 1,
                        "quantity": 0
                    }
                ]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/products/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

}

