package com.msa.commerce.orchestrator.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import com.msa.commerce.orchestrator.adapter.in.web.mapper.OrderStatusChangeMapper;
import com.msa.commerce.orchestrator.application.port.in.ChangeOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;
import com.msa.commerce.orchestrator.application.service.OrderNotFoundException;
import com.msa.commerce.orchestrator.domain.OrderStatus;

@WebMvcTest(
    controllers = OrderCommandController.class,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {OrderStatusChangeMapper.class}
    )
)
@DisplayName("OrderCommandController 통합 테스트")
class OrderCommandControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 주문 상태 변경 성공")
    void changeOrderStatus_Success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORDER-TEST-001";

        OrderStatusChangeResult result = new OrderStatusChangeResult(
            orderId,
            orderNumber,
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            LocalDateTime.now()
        );

        when(changeOrderStatusUseCase.changeOrderStatus(any(ChangeOrderStatusCommand.class)))
            .thenReturn(result);

        String requestBody = """
            {
                "newStatus": "CONFIRMED",
                "reason": "Customer confirmed order"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.orderNumber").value(orderNumber))
            .andExpect(jsonPath("$.previousStatus").value("PENDING"))
            .andExpect(jsonPath("$.currentStatus").value("CONFIRMED"))
            .andExpect(jsonPath("$.changedAt").isNotEmpty());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 주문이 존재하지 않음")
    void changeOrderStatus_OrderNotFound() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        when(changeOrderStatusUseCase.changeOrderStatus(any(ChangeOrderStatusCommand.class)))
            .thenThrow(new OrderNotFoundException("Order not found with ID: " + orderId));

        String requestBody = """
            {
                "newStatus": "CONFIRMED",
                "reason": "Customer confirmed order"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 잘못된 상태 전이")
    void changeOrderStatus_InvalidTransition() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        when(changeOrderStatusUseCase.changeOrderStatus(any(ChangeOrderStatusCommand.class)))
            .thenThrow(new IllegalStateException("Cannot change order status from PENDING to PROCESSING"));

        String requestBody = """
            {
                "newStatus": "PROCESSING",
                "reason": "Invalid transition"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Cannot change order status from PENDING to PROCESSING"));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 필수 필드 누락: newStatus")
    void changeOrderStatus_MissingNewStatus() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        String requestBody = """
            {
                "reason": "Customer confirmed order"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 필수 필드 누락: reason")
    void changeOrderStatus_MissingReason() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        String requestBody = """
            {
                "newStatus": "CONFIRMED"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 빈 reason")
    void changeOrderStatus_EmptyReason() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        String requestBody = """
            {
                "newStatus": "CONFIRMED",
                "reason": ""
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status - 잘못된 상태값")
    void changeOrderStatus_InvalidStatusValue() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        String requestBody = """
            {
                "newStatus": "INVALID_STATUS",
                "reason": "Test"
            }
            """;

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

}
