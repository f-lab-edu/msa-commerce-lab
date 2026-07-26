package com.msa.commerce.materializedview.application.port.in;

import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

public interface UpdateOrderViewUseCase {

    void applyOrderCreated(OrderCreatedView view);

    void applyStatusChanged(OrderStatusChangedView view);

}
