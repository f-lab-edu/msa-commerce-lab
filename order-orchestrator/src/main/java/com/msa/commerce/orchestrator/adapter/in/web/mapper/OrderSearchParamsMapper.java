package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import org.mapstruct.Mapper;

import com.msa.commerce.orchestrator.adapter.in.web.dto.request.OrderSearchParams;
import com.msa.commerce.orchestrator.application.port.in.OrderSearchCriteria;

@Mapper(componentModel = "spring")
public interface OrderSearchParamsMapper {

    OrderSearchCriteria toCriteria(OrderSearchParams params);

}
