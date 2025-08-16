package com.msa.commerce.monolith.product.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductResponse;
import com.msa.commerce.monolith.product.domain.ProductCategory;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductController 테스트
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController 테스트")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductCreateUseCase productCreateUseCase;

    @Test
    @DisplayName("정상적인 상품 생성 요청")
    void createProduct_Success() throws Exception {
        // given
        ProductCreateRequest request = createValidRequest();
        ProductResponse response = createProductResponse();

        given(productCreateUseCase.createProduct(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 상품"))
                .andExpect(jsonPath("$.description").value("테스트 상품 설명"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));
    }

    @Test
    @DisplayName("상품명이 없는 요청 - 400 Bad Request")
    void createProduct_EmptyName_BadRequest() throws Exception {
        // given
        ProductCreateRequest request = createValidRequest();
        // name을 null로 설정하기 위해 JSON 직접 작성
        String requestJson = """
                {
                    "description": "테스트 상품 설명",
                    "price": 10000,
                    "stockQuantity": 100,
                    "category": "ELECTRONICS",
                    "imageUrl": "http://example.com/image.jpg"
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
                    "name": "테스트 상품",
                    "description": "테스트 상품 설명",
                    "price": -1000,
                    "stockQuantity": 100,
                    "category": "ELECTRONICS",
                    "imageUrl": "http://example.com/image.jpg"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("재고 수량이 음수인 요청 - 400 Bad Request")
    void createProduct_NegativeStockQuantity_BadRequest() throws Exception {
        // given
        String requestJson = """
                {
                    "name": "테스트 상품",
                    "description": "테스트 상품 설명",
                    "price": 10000,
                    "stockQuantity": -1,
                    "category": "ELECTRONICS",
                    "imageUrl": "http://example.com/image.jpg"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리가 없는 요청 - 400 Bad Request")
    void createProduct_NoCategory_BadRequest() throws Exception {
        // given
        String requestJson = """
                {
                    "name": "테스트 상품",
                    "description": "테스트 상품 설명",
                    "price": 10000,
                    "stockQuantity": 100,
                    "imageUrl": "http://example.com/image.jpg"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복된 상품명으로 생성 시 예외 처리")
    void createProduct_DuplicateName_ThrowsException() throws Exception {
        // given
        ProductCreateRequest request = createValidRequest();
        
        given(productCreateUseCase.createProduct(any()))
                .willThrow(new IllegalArgumentException("이미 존재하는 상품명입니다: 테스트 상품"));

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private ProductCreateRequest createValidRequest() {
        ProductCreateRequest request = new ProductCreateRequest();
        // 리플렉션이나 테스트 전용 생성자를 사용하여 필드 설정
        // 여기서는 간단히 JSON 문자열로 테스트하는 방식을 선택
        return request;
    }

    private ProductResponse createProductResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(new BigDecimal("10000"))
                .stockQuantity(100)
                .category(ProductCategory.ELECTRONICS)
                .status(ProductStatus.ACTIVE)
                .imageUrl("http://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}