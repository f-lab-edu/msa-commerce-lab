package com.msa.commerce.orchestrator.application.service.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import com.msa.commerce.orchestrator.adapter.in.web.dto.response.PageResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderItemResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.in.response.OrderSummaryResponse;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.vo.ShippingAddress;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

    /**
     * Order를 record OrderResponse로 변환 (CreateOrderUseCase용)
     */
    default com.msa.commerce.orchestrator.application.port.in.OrderResponse toResponse(Order order) {
        List<com.msa.commerce.orchestrator.application.port.in.OrderItemResponse> orderItemResponses =
            order.getOrderItems().stream()
                .map(item -> com.msa.commerce.orchestrator.application.port.in.OrderItemResponse.builder()
                    .orderItemId(item.getOrderItemId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .productSku(item.getProductSku())
                    .productVariantId(item.getProductVariantId())
                    .variantName(item.getVariantName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .build()
                )
                .collect(Collectors.toList());

        return com.msa.commerce.orchestrator.application.port.in.OrderResponse.builder()
            .orderId(order.getOrderId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .orderDate(order.getOrderDate())
            .orderItems(orderItemResponses)
            .build();
    }

    @Mapping(target = "totalItemCount", expression = "java(order.getTotalItemCount())")
    @Mapping(source = "shippingAddress", target = "shippingAddress", qualifiedByName = "mapToShippingAddressMap")
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

    /**
     * ShippingAddress VO를 Map<String, Object>로 변환
     */
    @Named("mapToShippingAddressMap")
    default Map<String, Object> mapToShippingAddressMap(ShippingAddress address) {
        if (address == null) {
            return new HashMap<>();
        }

        Map<String, Object> map = new HashMap<>();
        if (address.addressType() != null) {
            map.put("addressType", address.addressType().name());
        }
        if (address.recipientName() != null) {
            map.put("recipientName", address.recipientName());
        }
        if (address.phoneNumber() != null) {
            map.put("phoneNumber", address.phoneNumber());
        }
        if (address.zipCode() != null) {
            map.put("zipCode", address.zipCode());
        }
        if (address.addressLine1() != null) {
            map.put("addressLine1", address.addressLine1());
        }
        if (address.addressLine2() != null) {
            map.put("addressLine2", address.addressLine2());
        }
        return map;
    }

}
