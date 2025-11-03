package com.msa.commerce.orchestrator.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderSearchParamsMapper;
import com.msa.commerce.orchestrator.application.port.in.GetOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.application.service.OrderNotFoundException;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@WebMvcTest(
    controllers = OrderQueryController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {OrderResponseMapper.class, OrderSearchParamsMapper.class}
    )
)
@DisplayName("OrderQueryController 통합 테스트")
class OrderQueryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetOrderUseCase getOrderUseCase;

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 ID로 단건 조회 성공")
    void getOrderById_Success() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse orderResponse = createTestOrderResponse(orderId, 1001L, OrderStatus.DELIVERED, new BigDecimal("100000"));

        when(getOrderUseCase.getOrderById(orderId)).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.customerId").value(1001))
            .andExpect(jsonPath("$.status").value("DELIVERED"))
            .andExpect(jsonPath("$.totalAmount").value(113000))
            .andExpect(jsonPath("$.currency").value("KRW"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 존재하지 않는 주문 조회 시 404 반환")
    void getOrderById_NotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();

        when(getOrderUseCase.getOrderById(orderId))
            .thenThrow(new OrderNotFoundException("주문을 찾을 수 없습니다. orderId: " + orderId));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/orders - 전체 주문 목록 조회 성공")
    void getOrders_Success() throws Exception {
        // Given
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), 1001L, OrderStatus.DELIVERED, new BigDecimal("100000")),
            createTestOrderSummary(UUID.randomUUID(), 1002L, OrderStatus.SHIPPED, new BigDecimal("50000")),
            createTestOrderSummary(UUID.randomUUID(), 1003L, OrderStatus.DELIVERED, new BigDecimal("200000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any())).thenReturn(summaryPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/orders?customerId=1001 - 고객 ID로 필터링 조회 성공")
    void getOrdersByCustomerId_Success() throws Exception {
        // Given
        Long customerId = 1001L;
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), customerId, OrderStatus.DELIVERED, new BigDecimal("100000")),
            createTestOrderSummary(UUID.randomUUID(), customerId, OrderStatus.SHIPPED, new BigDecimal("50000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any()))
            .thenReturn(summaryPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("customerId", "1001")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].customerId").value(1001))
            .andExpect(jsonPath("$.content[1].customerId").value(1001));
    }

    @Test
    @DisplayName("GET /api/v1/orders?status=DELIVERED - 주문 상태로 필터링 조회 성공")
    void getOrdersByStatus_Success() throws Exception {
        // Given
        OrderStatus status = OrderStatus.DELIVERED;
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), 1001L, status, new BigDecimal("100000")),
            createTestOrderSummary(UUID.randomUUID(), 1002L, status, new BigDecimal("200000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any()))
            .thenReturn(summaryPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("status", "DELIVERED")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].status").value("DELIVERED"))
            .andExpect(jsonPath("$.content[1].status").value("DELIVERED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders?customerId=1001&status=DELIVERED - 복합 필터링 조회 성공")
    void getOrdersByCustomerIdAndStatus_Success() throws Exception {
        // Given
        Long customerId = 1001L;
        OrderStatus status = OrderStatus.DELIVERED;
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), customerId, status, new BigDecimal("100000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any()))
            .thenReturn(summaryPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("customerId", "1001")
                .param("status", "DELIVERED")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].customerId").value(1001))
            .andExpect(jsonPath("$.content[0].status").value("DELIVERED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders?startDate=...&endDate=... - 날짜 범위로 필터링 조회 성공")
    void getOrdersByDateRange_Success() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 10, 21, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 10, 22, 23, 59);
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), 1001L, OrderStatus.DELIVERED, new BigDecimal("100000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any()))
            .thenReturn(summaryPage);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("startDate", startDate.format(formatter))
                .param("endDate", endDate.format(formatter))
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/orders?page=0&size=2&sort=totalAmount,asc - 페이지네이션 및 정렬 성공")
    void getOrders_WithPaginationAndSorting() throws Exception {
        // Given
        List<OrderSummaryResponse> summaries = List.of(
            createTestOrderSummary(UUID.randomUUID(), 1001L, OrderStatus.DELIVERED, new BigDecimal("50000")),
            createTestOrderSummary(UUID.randomUUID(), 1002L, OrderStatus.SHIPPED, new BigDecimal("100000"))
        );
        Page<OrderSummaryResponse> summaryPage = new PageImpl<>(summaries);

        when(getOrderUseCase.searchOrders(any())).thenReturn(summaryPage);

        // When & Then
        mockMvc.perform(get("/api/v1/orders")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "totalAmount,asc")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2));
    }

    // Helper method to create test order response
    private OrderResponse createTestOrderResponse(UUID orderId, Long customerId, OrderStatus status, BigDecimal subtotalAmount) {
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipientName", "김철수");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("zipCode", "06234");
        shippingAddress.put("address", "서울특별시 강남구 테헤란로 123");

        BigDecimal taxAmount = subtotalAmount.multiply(new BigDecimal("0.1"));
        BigDecimal shippingAmount = new BigDecimal("3000");
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotalAmount.add(taxAmount).add(shippingAmount).subtract(discountAmount);

        return OrderResponse.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251025-0001")
            .customerId(customerId)
            .status(status)
            .subtotalAmount(subtotalAmount)
            .taxAmount(taxAmount)
            .shippingAmount(shippingAmount)
            .discountAmount(discountAmount)
            .totalAmount(totalAmount)
            .currency("KRW")
            .shippingAddress(shippingAddress)
            .orderDate(LocalDateTime.of(2025, 10, 20, 10, 30))
            .sourceChannel("WEB")
            .totalItemCount(0)
            .orderItems(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // Helper method to create test order summary
    private OrderSummaryResponse createTestOrderSummary(UUID orderId, Long customerId, OrderStatus status, BigDecimal subtotalAmount) {
        BigDecimal taxAmount = subtotalAmount.multiply(new BigDecimal("0.1"));
        BigDecimal shippingAmount = new BigDecimal("3000");
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotalAmount.add(taxAmount).add(shippingAmount).subtract(discountAmount);

        return OrderSummaryResponse.builder()
            .orderId(orderId)
            .orderNumber("ORD-20251025-0001")
            .customerId(customerId)
            .status(status)
            .totalAmount(totalAmount)
            .currency("KRW")
            .totalItemCount(0)
            .orderDate(LocalDateTime.of(2025, 10, 20, 10, 30))
            .sourceChannel("WEB")
            .build();
    }

}
