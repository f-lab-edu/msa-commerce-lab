package com.msa.commerce.orchestrator.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusCommand;
import com.msa.commerce.orchestrator.application.port.in.UpdateOrderStatusUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.PublishOrderEventPort;
import com.msa.commerce.orchestrator.domain.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    private final PublishOrderEventPort publishOrderEventPort;

    @Override
    @Transactional
    public void updateOrderStatus(UpdateOrderStatusCommand command) {
        Order order = orderRepository.findByOrderId(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(
                "Order not found with id: " + command.orderId()));

        order.updateStatus(command.newStatus());

        orderRepository.save(order);

        publishOrderEventPort.publishOrderStatusChangedEvent(order);
    }

}
