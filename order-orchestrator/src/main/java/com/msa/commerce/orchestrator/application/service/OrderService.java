package com.msa.commerce.orchestrator.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.util.UuidGenerator;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderCommand;
import com.msa.commerce.orchestrator.application.port.in.CreateOrderUseCase;
import com.msa.commerce.orchestrator.application.port.in.OrderResponse;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.application.service.mapper.OrderResponseMapper;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderItem;
import com.msa.commerce.orchestrator.domain.vo.ProductInfo;
import com.msa.commerce.orchestrator.domain.vo.ShippingAddress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    private final ProductPort productPort;

    private final OrderResponseMapper orderResponseMapper;

    @Override
    public OrderResponse createOrder(CreateOrderCommand command) {
        String orderNumber = UuidGenerator.generate().toString();

        // Value Object 사용으로 타입 안정성 확보
        ShippingAddress defaultShippingAddress = ShippingAddress.createDefault();

        Order order = Order.create(
            orderNumber,
            command.customerId(),
            defaultShippingAddress,
            "WEB"
        );

        // Port를 통해 상품 정보 조회
        command.orderItems().forEach(itemCommand -> {
            ProductInfo productInfo = productPort.getProductInfo(itemCommand.productId());

            OrderItem orderItem = OrderItem.create(
                productInfo.productId(),
                productInfo.productName(),
                productInfo.sku(),
                null,  // productVariantId: 향후 상품 변형 기능 추가 시 사용
                null,  // variantName: 향후 상품 변형 기능 추가 시 사용
                itemCommand.quantity(),
                itemCommand.unitPrice()
            );
            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        return orderResponseMapper.toResponse(savedOrder);
    }

}
