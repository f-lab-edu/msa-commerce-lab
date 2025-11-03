package com.msa.commerce.orchestrator.adapter.out.persistence;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.orchestrator.domain.AddressType;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.vo.ShippingAddress;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderDomainMapper {

    List<OrderItem> toOrderItemDomainList(List<OrderItemJpaEntity> entities);

    @Mapping(source = "orderItemId", target = "orderItemId")
    OrderItem toOrderItemDomain(OrderItemJpaEntity entity);

    @Mapping(source = "orderId", target = "orderId")
    @Mapping(source = "customerId", target = "customerId")
    @Mapping(source = "orderItems", target = "orderItems")
    @Mapping(source = "shippingAddress", target = "shippingAddress", qualifiedByName = "mapToShippingAddress")
    Order toDomain(OrderJpaEntity entity);

    /**
     * Map<String, Object>를 ShippingAddress VO로 변환
     */
    @Named("mapToShippingAddress")
    default ShippingAddress mapToShippingAddress(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return ShippingAddress.createDefault();
        }

        AddressType addressType = AddressType.DEFAULT;
        if (map.get("addressType") != null) {
            String typeStr = map.get("addressType").toString();
            try {
                addressType = AddressType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                addressType = AddressType.DEFAULT;
            }
        }

        return ShippingAddress.create(
            addressType,
            map.get("recipientName") != null ? map.get("recipientName").toString() : null,
            map.get("phoneNumber") != null ? map.get("phoneNumber").toString() : null,
            map.get("zipCode") != null ? map.get("zipCode").toString() : null,
            map.get("addressLine1") != null ? map.get("addressLine1").toString() : null,
            map.get("addressLine2") != null ? map.get("addressLine2").toString() : null
        );
    }

}
