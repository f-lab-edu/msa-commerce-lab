package com.msa.commerce.orchestrator.domain.state;

import java.util.EnumMap;
import java.util.Map;

import com.msa.commerce.orchestrator.domain.OrderStatus;

public class OrderStateFactory {

    private static final Map<OrderStatus, OrderState> STATE_MAP = new EnumMap<>(OrderStatus.class);

    static {
        STATE_MAP.put(OrderStatus.PENDING, new PendingOrderState());
        STATE_MAP.put(OrderStatus.CONFIRMED, new ConfirmedOrderState());
        STATE_MAP.put(OrderStatus.PAYMENT_PENDING, new PaymentPendingOrderState());
        STATE_MAP.put(OrderStatus.PAID, new PaidOrderState());
        STATE_MAP.put(OrderStatus.PROCESSING, new ProcessingOrderState());
        STATE_MAP.put(OrderStatus.SHIPPED, new ShippedOrderState());
        STATE_MAP.put(OrderStatus.DELIVERED, new DeliveredOrderState());
        STATE_MAP.put(OrderStatus.CANCELLED, new CancelledOrderState());
    }

    private OrderStateFactory() {
    }

    public static OrderState getState(OrderStatus status) {
        OrderState state = STATE_MAP.get(status);
        if (state == null) {
            throw new IllegalArgumentException("Unsupported order status: " + status);
        }
        return state;
    }

}
