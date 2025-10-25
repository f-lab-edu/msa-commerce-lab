package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.msa.commerce.orchestrator.adapter.in.web.dto.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.OrderItemRequest;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;

@DisplayName("OrderMapper 테스트")
class OrderMapperTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    @DisplayName("CreateOrderRequest를 CreateOrderCommand로 변환")
    void toCommand() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
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

        // when
        CreateOrderCommand command = orderMapper.toCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(1L);
        assertThat(command.orderItems()).hasSize(2);

        assertThat(command.orderItems().get(0).productId()).isEqualTo(101L);
        assertThat(command.orderItems().get(0).quantity()).isEqualTo(2);
        assertThat(command.orderItems().get(0).unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));

        assertThat(command.orderItems().get(1).productId()).isEqualTo(102L);
        assertThat(command.orderItems().get(1).quantity()).isEqualTo(1);
        assertThat(command.orderItems().get(1).unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(25000));
    }

    @Test
    @DisplayName("CreateOrderRequest를 CreateOrderCommand로 변환 - 단일 아이템")
    void toCommand_SingleItem() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(1L)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(101L)
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(50000))
                    .build()
            ))
            .build();

        // when
        CreateOrderCommand command = orderMapper.toCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(1L);
        assertThat(command.orderItems()).hasSize(1);
        assertThat(command.orderItems().get(0).productId()).isEqualTo(101L);
        assertThat(command.orderItems().get(0).quantity()).isEqualTo(5);
        assertThat(command.orderItems().get(0).unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("CreateOrderRequest를 CreateOrderCommand로 변환 - 여러 아이템")
    void toCommand_MultipleItems() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
            .customerId(100L)
            .orderItems(List.of(
                OrderItemRequest.builder()
                    .productId(1L)
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1000))
                    .build(),
                OrderItemRequest.builder()
                    .productId(2L)
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(2000))
                    .build(),
                OrderItemRequest.builder()
                    .productId(3L)
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(3000))
                    .build()
            ))
            .build();

        // when
        CreateOrderCommand command = orderMapper.toCommand(request);

        // then
        assertThat(command).isNotNull();
        assertThat(command.customerId()).isEqualTo(100L);
        assertThat(command.orderItems()).hasSize(3);
    }
}
