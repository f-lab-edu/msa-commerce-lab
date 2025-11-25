package com.msa.commerce.orchestrator.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.ConfirmOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.command.ConfirmOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.domain.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmOrderService implements ConfirmOrderUseCase {

    private final OrderRepository orderRepository;

    private final OrderResponseMapper orderResponseMapper;

    @Override
    public OrderResponse confirm(ConfirmOrderCommand command) {
        Order order = orderRepository.findByOrderId(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(String.format("Order not found with ID: %s", command.orderId())));

        order.confirm();

        orderRepository.save(order);

        return orderResponseMapper.toOrderResponse(order);
    }

}
