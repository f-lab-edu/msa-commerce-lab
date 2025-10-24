package com.msa.commerce.orchestrator.adapter.in.web.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderItemResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.adapter.in.web.dto.response.PageResponse;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

    @Mapping(target = "totalItemCount", expression = "java(order.getTotalItemCount())")
    OrderResponse toOrderResponse(Order order);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);

    @Mapping(target = "totalItemCount", expression = "java(order.getTotalItemCount())")
    OrderSummaryResponse toOrderSummaryResponse(Order order);

    List<OrderSummaryResponse> toOrderSummaryResponseList(List<Order> orders);

    default <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
    }

}
