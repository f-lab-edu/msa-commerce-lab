package com.msa.commerce.orchestrator.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;
import com.msa.commerce.orchestrator.domain.vo.VerifiedProduct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    private final ProductPort productPort;

    @Override
    @Transactional
    public UUID createOrder(CreateOrderCommand command) {
        ProductVerification verification = verifyProducts(command);

        Order order = newOrder(command);
        command.getOrderItems()
            .forEach(item -> order.addOrderItem(toOrderItem(item, verification.find(item.getProductId()))));

        Order savedOrder = orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(savedOrder);

        return savedOrder.getOrderId();
    }

    private ProductVerification verifyProducts(CreateOrderCommand command) {
        List<ProductVerificationItem> items = command.getOrderItems().stream()
            .map(item -> new ProductVerificationItem(item.getProductId(), item.getQuantity()))
            .toList();

        ProductVerification verification = productPort.verify(items);
        if (!verification.allAvailable()) {
            throw new ProductUnavailableException(describeUnavailable(verification));
        }
        return verification;
    }

    private String describeUnavailable(ProductVerification verification) {
        return verification.unavailableProducts().stream()
            .map(product -> "productId=%d(%s)".formatted(product.productId(), product.unavailableReason()))
            .collect(Collectors.joining(", ", "Products are not available for order: ", ""));
    }

    private Order newOrder(CreateOrderCommand command) {
        return Order.create(
            command.getOrderNumber(),
            command.getCustomerId(),
            command.getShippingAddress(),
            command.getSourceChannel()
        );
    }

    private OrderItem toOrderItem(CreateOrderCommand.OrderItemCommand item, VerifiedProduct product) {
        return OrderItem.create(
            product.productId(),
            product.name(),
            product.sku(),
            item.getProductVariantId(),
            item.getVariantName(),
            item.getQuantity(),
            product.currentPrice()
        );
    }

}
