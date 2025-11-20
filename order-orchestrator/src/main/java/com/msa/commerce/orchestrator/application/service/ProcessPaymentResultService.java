package com.msa.commerce.orchestrator.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.orchestrator.application.port.in.ProcessPaymentResultUseCase;
import com.msa.commerce.orchestrator.application.port.out.OrderEventPublisher;
import com.msa.commerce.orchestrator.application.port.out.OrderRepository;
import com.msa.commerce.orchestrator.domain.Order;
import com.msa.commerce.orchestrator.domain.OrderStatus;
import com.msa.commerce.orchestrator.domain.event.PaymentResultEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessPaymentResultService implements ProcessPaymentResultUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentResultService.class);

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public void processPaymentResult(PaymentResultEvent event) {
        log.info("Processing payment result: orderId={}, paymentStatus={}, paymentId={}",
            event.getOrderId(), event.getPaymentStatus(), event.getPaymentId());

        Order order = orderRepository.findByOrderId(event.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(
                "Order not found for payment result: " + event.getOrderId()
            ));

        OrderStatus previousStatus = order.getStatus();

        switch (event.getPaymentStatus()) {
            case SUCCESS -> handlePaymentSuccess(order, event);
            case FAILED -> handlePaymentFailure(order, event);
            case CANCELLED -> handlePaymentCancellation(order, event);
            case PENDING -> log.warn("Received PENDING payment status for orderId: {}, no action taken",
                event.getOrderId());
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated after payment result: orderId={}, previousStatus={}, currentStatus={}",
            updatedOrder.getOrderId(), previousStatus, updatedOrder.getStatus());

        if (previousStatus != updatedOrder.getStatus()) {
            String reason = buildStatusChangeReason(event);
            orderEventPublisher.publishOrderUpdated(
                updatedOrder,
                previousStatus,
                reason,
                event.getMetadata().getCorrelationId()
            );
        }
    }

    private void handlePaymentSuccess(Order order, PaymentResultEvent event) {
        log.info("Handling successful payment for orderId: {}, paymentId: {}",
            order.getOrderId(), event.getPaymentId());

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            log.warn("Order is not in PAYMENT_PENDING status, current status: {}, orderId: {}",
                order.getStatus(), order.getOrderId());
        }

        order.markPaymentCompleted();
        log.info("Order payment completed: orderId={}, paymentId={}, transactionId={}",
            order.getOrderId(), event.getPaymentId(), event.getTransactionId());
    }

    private void handlePaymentFailure(Order order, PaymentResultEvent event) {
        log.warn("Handling failed payment for orderId: {}, paymentId: {}, reason: {}",
            order.getOrderId(), event.getPaymentId(), event.getFailureReason());

        if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
            order.cancel();
            log.info("Order cancelled due to payment failure: orderId={}, reason={}",
                order.getOrderId(), event.getFailureReason());
        } else {
            log.warn("Order is not in PAYMENT_PENDING status, cannot cancel. Current status: {}, orderId: {}",
                order.getStatus(), order.getOrderId());
        }
    }

    private void handlePaymentCancellation(Order order, PaymentResultEvent event) {
        log.info("Handling cancelled payment for orderId: {}, paymentId: {}",
            order.getOrderId(), event.getPaymentId());

        if (order.getStatus().canBeCancelled()) {
            order.cancel();
            log.info("Order cancelled due to payment cancellation: orderId={}", order.getOrderId());
        } else {
            log.warn("Order cannot be cancelled, current status: {}, orderId: {}",
                order.getStatus(), order.getOrderId());
        }
    }

    private String buildStatusChangeReason(PaymentResultEvent event) {
        return String.format("Payment %s - PaymentId: %s, TransactionId: %s%s",
            event.getPaymentStatus(),
            event.getPaymentId(),
            event.getTransactionId() != null ? event.getTransactionId() : "N/A",
            event.getFailureReason() != null ? ", Reason: " + event.getFailureReason() : ""
        );
    }

}
