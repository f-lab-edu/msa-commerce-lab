package com.msa.commerce.monolith.product.infrastructure.event;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.product.application.service.ProductEventPublisher;
import com.msa.commerce.monolith.product.domain.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductEventPublisherImpl implements ProductEventPublisher {

    @Override
    public void publishProductDeletedEvent(Product product) {
        // TODO: 실제 이벤트 발행 로직 구현
        // 예: Kafka, RabbitMQ, SNS 등으로 이벤트 전송
        log.info("Publishing product deleted event for productId: {}, sku: {}",
            product.getId(), product.getSku());

        // 외부 시스템으로 이벤트 전송 로직
        // 예시:
        // kafkaProducer.send("product.deleted", productDeletedEvent);
        // rabbitTemplate.convertAndSend("product.exchange", "product.deleted", event);
    }

}
