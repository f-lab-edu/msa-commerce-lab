package com.msa.commerce.monolith.product.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.service.ProductEventPublisher;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.event.ProductDeletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisherImpl implements ProductEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishProductDeletedEvent(Product product) {
        ProductDeletedEvent event = new ProductDeletedEvent(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDeletedAt()
        );

        eventPublisher.publishEvent(event);
    }

}
