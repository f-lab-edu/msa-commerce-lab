package com.msa.commerce.orchestrator.adapter.out.product;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.domain.vo.ProductInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

    private final RestClient productRestClient;

    @Override
    public ProductInfo getProductInfo(Long productId) {
        try {
            ProductResponse response = productRestClient.get()
                .uri("/products/{productId}", productId)
                .retrieve()
                .body(ProductResponse.class);

            if (response == null) {
                throw new ProductNotFoundException("Product not found for productId: " + productId);
            }

            return response.toProductInfo();
        } catch (Exception e) {
            log.error("Failed to get product info for productId={}", productId, e);
            throw new ProductServiceException("Failed to retrieve product information", e);
        }
    }

}
