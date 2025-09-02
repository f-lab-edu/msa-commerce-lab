package com.msa.commerce.monolith.product.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.msa.commerce.common.monitoring.MetricsCollector;
import com.msa.commerce.monolith.product.application.port.in.ProductCreateUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductGetUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductPageResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductSearchUseCase;
import com.msa.commerce.monolith.product.application.port.in.ProductUpdateUseCase;
import com.msa.commerce.monolith.product.domain.ProductStatus;
import com.msa.commerce.monolith.product.domain.ProductType;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController 검색 API 테스트")
class ProductControllerSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCreateUseCase productCreateUseCase;

    @MockitoBean
    private ProductGetUseCase productGetUseCase;

    @MockitoBean
    private ProductUpdateUseCase productUpdateUseCase;

    @MockitoBean
    private ProductSearchUseCase productSearchUseCase;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private MetricsCollector metricsCollector;

    @Test
    @DisplayName("상품 검색 API가 정상적으로 동작한다")
    @WithMockUser
    void getProducts_Success() throws Exception {
        // Given
        ProductPageResponse pageResponse = createMockProductPageResponse();
        ProductSearchCommand searchCommand = createMockSearchCommand();

        given(productMapper.toSearchCommand(any(ProductSearchRequest.class))).willReturn(searchCommand);
        given(productGetUseCase.searchProducts(any(ProductSearchCommand.class))).willReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("categoryId", "1")
                .param("page", "0")
                .param("size", "20")
                .param("sortProperty", "createdAt")
                .param("sortDirection", "DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpectAll(
                jsonPath("$.content").isArray(),
                jsonPath("$.content.length()").value(2),
                // Product 1 검증
                jsonPath("$.content[0].id").value(1),
                jsonPath("$.content[0].name").value("Test Product 1"),
                jsonPath("$.content[0].basePrice").value(100.00), // price -> basePrice 수정
                jsonPath("$.content[0].viewCount").value(5),
                // Product 2 검증
                jsonPath("$.content[1].id").value(2),
                jsonPath("$.content[1].name").value("Test Product 2"),
                jsonPath("$.content[1].basePrice").value(200.00), // price -> basePrice 수정
                jsonPath("$.content[1].viewCount").value(10),
                // 페이징 정보 검증
                jsonPath("$.page").value(0),
                jsonPath("$.size").value(20),
                jsonPath("$.totalElements").value(2),
                jsonPath("$.totalPages").value(1),
                jsonPath("$.first").value(true),
                jsonPath("$.last").value(true),
                jsonPath("$.hasNext").value(false),
                jsonPath("$.hasPrevious").value(false)
            );
    }

    @Test
    @DisplayName("가격 범위 필터링이 정상적으로 동작한다")
    @WithMockUser
    void getProducts_WithPriceRange() throws Exception {
        // Given
        ProductPageResponse pageResponse = ProductPageResponse.builder()
            .content(Arrays.asList())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        ProductSearchCommand searchCommand = ProductSearchCommand.builder()
            .minPrice(new BigDecimal("50.00"))
            .maxPrice(new BigDecimal("150.00"))
            .page(0)
            .size(20)
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
            .build();

        given(productMapper.toSearchCommand(any(ProductSearchRequest.class))).willReturn(searchCommand);
        given(productGetUseCase.searchProducts(any(ProductSearchCommand.class))).willReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 상태 필터링이 정상적으로 동작한다")
    @WithMockUser
    void getProducts_WithStatus() throws Exception {
        // Given
        ProductPageResponse pageResponse = ProductPageResponse.builder()
            .content(Arrays.asList())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        ProductSearchCommand searchCommand = ProductSearchCommand.builder()
            .status(ProductStatus.ACTIVE)
            .page(0)
            .size(20)
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
            .build();

        given(productMapper.toSearchCommand(any(ProductSearchRequest.class))).willReturn(searchCommand);
        given(productGetUseCase.searchProducts(any(ProductSearchCommand.class))).willReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("status", "ACTIVE"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정렬 옵션이 정상적으로 동작한다")
    @WithMockUser
    void getProducts_WithSorting() throws Exception {
        // Given
        ProductPageResponse pageResponse = ProductPageResponse.builder()
            .content(Arrays.asList())
            .page(0)
            .size(20)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        ProductSearchCommand searchCommand = ProductSearchCommand.builder()
            .page(0)
            .size(20)
            .sortProperty("price")
            .sortDirection(Sort.Direction.ASC)
            .build();

        given(productMapper.toSearchCommand(any(ProductSearchRequest.class))).willReturn(searchCommand);
        given(productGetUseCase.searchProducts(any(ProductSearchCommand.class))).willReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products")
                .param("sortProperties", "price")
                .param("sortDirection", "ASC"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    private ProductPageResponse createMockProductPageResponse() {
        return ProductPageResponse.builder()
            .content(Arrays.asList(
                createMockProduct(1L, "TEST-001", "Test Product 1", "100.00", 5L, 1),
                createMockProduct(2L, "TEST-002", "Test Product 2", "200.00", 10L, 2)
            ))
            .page(0)
            .size(20)
            .totalElements(2L)
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
    }

    private ProductSearchResponse createMockProduct(Long id, String sku, String name,
        String price, Long viewCount, int daysAgo) {
        return ProductSearchResponse.builder()
            .id(id)
            .sku(sku)
            .name(name)
            .shortDescription("Short description " + id)
            .description("Test Description " + id)
            .categoryId(1L)
            .brand("TestBrand")
            .productType(ProductType.PHYSICAL)
            .status(ProductStatus.ACTIVE)
            .basePrice(new BigDecimal(price))
            .currency("KRW")
            .requiresShipping(true)
            .isTaxable(true)
            .isFeatured(id == 2L)
            .slug("test-product-" + id)
            .version(1L)
            .viewCount(viewCount)
            .createdAt(LocalDateTime.now().minusDays(daysAgo))
            .updatedAt(LocalDateTime.now())
            .build();
    }

    private ProductSearchCommand createMockSearchCommand() {
        return ProductSearchCommand.builder()
            .categoryId(1L)
            .page(0)
            .size(20)
            .sortProperty("createdAt")
            .sortDirection(Sort.Direction.DESC)
            .build();
    }

}
