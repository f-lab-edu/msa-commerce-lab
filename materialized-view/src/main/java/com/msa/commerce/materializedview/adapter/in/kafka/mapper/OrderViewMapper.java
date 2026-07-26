package com.msa.commerce.materializedview.adapter.in.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderCreatedEvent;
import com.msa.commerce.materializedview.adapter.in.kafka.dto.OrderUpdatedEvent;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

@Mapper(componentModel = "spring")
public interface OrderViewMapper {

    @Mapping(source = "metadata.eventId", target = "eventId")
    OrderCreatedView toView(OrderCreatedEvent event);

    @Mapping(source = "metadata.eventId", target = "eventId")
    OrderStatusChangedView toView(OrderUpdatedEvent event);

}
