package com.msa.commerce.materializedview.application.port.out;

import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

public interface OrderViewRepository {

    void applyOrderCreated(OrderCreatedView view);

    void applyStatusChanged(OrderStatusChangedView view);

}
