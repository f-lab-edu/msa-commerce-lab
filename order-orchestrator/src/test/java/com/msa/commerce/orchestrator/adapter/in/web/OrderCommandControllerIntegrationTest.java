package com.msa.commerce.orchestrator.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.UpdateOrderStatusRequest;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderCommandMapper;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.service.OrderNotFoundException;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@WebMvcTest(
    controllers = OrderCommandController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {GlobalExceptionHandler.class, OrderCommandMapper.class}
    )
)
@DisplayName("OrderCommandController 통합 테스트")
class OrderCommandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 성공")
    void createOrder_Success() throws Exception {
        UUID orderId = UUID.randomUUID();

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("recipientName", "김철수");
        shippingAddress.put("phone", "010-1234-5678");
        shippingAddress.put("zipCode", "06234");
        shippingAddress.put("address", "서울특별시 강남구 테헤란로 123");

        CreateOrderRequest.OrderItemRequest orderItem = CreateOrderRequest.OrderItemRequest.builder()
            .productId(1001L)
            .productName("테스트 상품")
            .productSku("TEST-SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(10000))
            .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
            .orderNumber("ORD-20240115-001")
            .customerId(12345L)
            .shippingAddress(shippingAddress)
            .sourceChannel("WEB")
            .orderItems(List.of(orderItem))
            .build();

        when(createOrderUseCase.createOrder(any())).thenReturn(orderId);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        verify(createOrderUseCase, times(1)).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - 필수 필드 누락 시 400 반환")
    void createOrder_BadRequest_MissingRequiredFields() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(12345L)
            .build();

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 주문 상태 변경 성공")
    void updateOrderStatus_Success() throws Exception {
        UUID orderId = UUID.randomUUID();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        doNothing().when(updateOrderStatusUseCase).updateOrderStatus(any());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("주문 상태가 성공적으로 변경되었습니다."));

        verify(updateOrderStatusUseCase, times(1)).updateOrderStatus(any());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 존재하지 않는 주문 ID로 상태 변경 시 404 반환")
    void updateOrderStatus_NotFound() throws Exception {
        UUID orderId = UUID.randomUUID();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        doThrow(new OrderNotFoundException("Order not found with id: " + orderId))
            .when(updateOrderStatusUseCase).updateOrderStatus(any());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(updateOrderStatusUseCase, times(1)).updateOrderStatus(any());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 잘못된 상태 전환 시 400 반환")
    void updateOrderStatus_BadRequest_InvalidTransition() throws Exception {
        UUID orderId = UUID.randomUUID();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
            .newStatus(OrderStatus.CONFIRMED)
            .build();

        doThrow(new IllegalStateException("Cannot confirm order with no items"))
            .when(updateOrderStatusUseCase).updateOrderStatus(any());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(updateOrderStatusUseCase, times(1)).updateOrderStatus(any());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 필수 필드 누락 시 400 반환")
    void updateOrderStatus_BadRequest_MissingRequiredFields() throws Exception {
        UUID orderId = UUID.randomUUID();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
            .build();

        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(updateOrderStatusUseCase, never()).updateOrderStatus(any());
    }

}
