package com.msa.commerce.orchestrator.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.ChangeOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.port.in.command.ChangeOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.response.OrderStatusChangeResult;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeOrderStatusService implements ChangeOrderStatusUseCase {

    private final OrderRepository orderRepository;

    @Override
    public OrderStatusChangeResult changeOrderStatus(ChangeOrderStatusCommand command) {
        Order order = orderRepository.findByOrderId(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(
                String.format("Order not found with ID: %s", command.orderId())
            ));

        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(command.newStatus(), command.reason());

        orderRepository.save(order);

        return new OrderStatusChangeResult(
            order.getOrderId(),
            order.getOrderNumber(),
            previousStatus,
            order.getStatus(),
            LocalDateTime.now()
        );
    }

}
