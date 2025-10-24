package com.msa.commerce.orchestrator.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.orchestrator.adapter.in.web.dto.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.OrderItemRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.OrderItemResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.OrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderDtoMapper;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("OrderController 통합 테스트")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private OrderDtoMapper orderDtoMapper;

    @Test
    @DisplayName("POST /api/v1/orders - 정상적으로 주문 생성")
    void createOrder_Success() throws Exception {
        // given
        Long customerId = 1L;
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(customerId)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(101L)
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .build(),
                OrderItemRequest.builder()
                    .productId(102L)
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(25000))
                    .build()
            ))
            .build();

        UUID orderId = UUID.randomUUID();
        Order mockOrder = mock(Order.class);
        when(mockOrder.getOrderId()).thenReturn(orderId);

        OrderResponse expectedResponse = OrderResponse.builder()
            .orderId(orderId)
            .orderNumber("ORD-20250101120000-0001")
            .customerId(customerId)
            .status(OrderStatus.PENDING)
            .totalAmount(BigDecimal.valueOf(45000))
            .currency("KRW")
            .orderDate(LocalDateTime.now())
            .orderItems(List.of(
                OrderItemResponse.builder()
                    .productId(101L)
                    .productName("Product-101")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .totalPrice(BigDecimal.valueOf(20000))
                    .build(),
                OrderItemResponse.builder()
                    .productId(102L)
                    .productName("Product-102")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(25000))
                    .totalPrice(BigDecimal.valueOf(25000))
                    .build()
            ))
            .build();

        when(createOrderUseCase.createOrder(any())).thenReturn(mockOrder);
        when(orderDtoMapper.toOrderResponse(any())).thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.orderNumber").value("ORD-20250101120000-0001"))
            .andExpect(jsonPath("$.customerId").value(customerId))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(45000))
            .andExpect(jsonPath("$.currency").value("KRW"))
            .andExpect(jsonPath("$.orderItems").isArray())
            .andExpect(jsonPath("$.orderItems.length()").value(2));

        verify(createOrderUseCase, times(1)).createOrder(any());
        verify(orderDtoMapper, times(1)).toOrderResponse(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - customerId가 null인 경우 400 반환")
    void createOrder_NullCustomerId() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(null)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(101L)
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .build()
            ))
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").value("customerId"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("Customer ID is required"));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - orderItems가 비어있는 경우 400 반환")
    void createOrder_EmptyOrderItems() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .orderItems(List.of())
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").value("orderItems"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("Order items cannot be empty"));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - unitPrice가 0인 경우 400 반환")
    void createOrder_InvalidUnitPrice() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(101L)
                    .quantity(1)
                    .unitPrice(BigDecimal.ZERO)
                    .build()
            ))
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").value("orderItems[0].unitPrice"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("Unit price must be greater than 0"));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - quantity가 0 이하인 경우 400 반환")
    void createOrder_InvalidQuantity() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(101L)
                    .quantity(0)
                    .unitPrice(BigDecimal.valueOf(10000))
                    .build()
            ))
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").value("orderItems[0].quantity"))
            .andExpect(jsonPath("$.validationErrors[0].message").value("Quantity must be at least 1"));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/v1/orders - 잘못된 JSON 형식인 경우 400 반환")
    void createOrder_InvalidJsonFormat() throws Exception {
        // given
        String invalidJson = "{invalid json}";

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request body format"));

        verify(createOrderUseCase, never()).createOrder(any());
    }
}
