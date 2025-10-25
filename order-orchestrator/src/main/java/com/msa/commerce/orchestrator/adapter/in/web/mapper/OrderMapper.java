package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.common.aop.ValidateResult;
import com.msa.commerce.orchestrator.adapter.in.web.dto.CreateOrderRequest;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @ValidateResult
    CreateOrderCommand toCommand(CreateOrderRequest request);

}
