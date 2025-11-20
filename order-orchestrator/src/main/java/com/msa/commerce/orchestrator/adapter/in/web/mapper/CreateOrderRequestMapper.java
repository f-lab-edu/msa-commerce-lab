package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.CreateOrderRequest;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;

@Mapper(componentModel = "spring")
public interface CreateOrderRequestMapper {

    @Mapping(target = "orderItems", source = "orderItems")
    CreateOrderCommand toCommand(CreateOrderRequest request);

    CreateOrderCommand.OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemRequest request);

}
