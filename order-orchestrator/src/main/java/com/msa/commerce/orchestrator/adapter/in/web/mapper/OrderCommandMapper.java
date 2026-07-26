package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CancelOrderRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.command.CancelOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.command.ConfirmOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.command.PayOrderCommand;

@Mapper(componentModel = "spring")
public interface OrderCommandMapper {

    default ConfirmOrderCommand toConfirmCommand(UUID orderId) {
        return new ConfirmOrderCommand(orderId);
    }

    default PayOrderCommand toPayCommand(UUID orderId) {
        return new PayOrderCommand(orderId);
    }

    default CancelOrderCommand toCancelCommand(UUID orderId, CancelOrderRequest request) {
        return new CancelOrderCommand(orderId, request.reason());
    }

    @Mapping(target = "orderItems", source = "orderItems")
    CreateOrderCommand toCreateOrderCommand(CreateOrderRequest request);

    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

}
