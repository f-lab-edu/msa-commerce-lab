package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.UpdateOrderStatusRequest;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusCommand;

@Mapper(componentModel = "spring")
public interface OrderCommandMapper {

    @Mapping(target = "orderItems", source = "orderItems")
    CreateOrderCommand toCreateOrderCommand(CreateOrderRequest request);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productSku", source = "productSku")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest item);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "newStatus", source = "request.newStatus")
    UpdateOrderStatusCommand toUpdateOrderStatusCommand(UUID orderId, UpdateOrderStatusRequest request);

}
