package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.OrderStatusChangeRequest;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderStatusChangeResponse;
import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderStatusChangeMapper {

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "newStatus", source = "request.newStatus")
    @Mapping(target = "reason", source = "request.reason")
    ChangeOrderStatusCommand toCommand(UUID orderId, OrderStatusChangeRequest request);

    OrderStatusChangeResponse toResponse(OrderStatusChangeResult result);

}
