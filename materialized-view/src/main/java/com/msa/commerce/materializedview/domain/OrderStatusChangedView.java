package com.msa.commerce.materializedview.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusChangedView(
    String eventId,
    UUID orderId,
    Long customerId,
    String previousStatus,
    String currentStatus,
    LocalDateTime statusChangedAt
) {

    public boolean hasCategoryChanged() {
        return previousCategory() != currentCategory();
    }

    public int pendingDelta() {
        return deltaOf(OrderStatusCategory.ACTIVE);
    }

    public int completedDelta() {
        return deltaOf(OrderStatusCategory.COMPLETED);
    }

    public int cancelledDelta() {
        return deltaOf(OrderStatusCategory.CANCELLED);
    }

    private int deltaOf(OrderStatusCategory category) {
        return (currentCategory() == category ? 1 : 0) - (previousCategory() == category ? 1 : 0);
    }

    private OrderStatusCategory previousCategory() {
        return OrderStatusCategory.from(previousStatus);
    }

    private OrderStatusCategory currentCategory() {
        return OrderStatusCategory.from(currentStatus);
    }

}
