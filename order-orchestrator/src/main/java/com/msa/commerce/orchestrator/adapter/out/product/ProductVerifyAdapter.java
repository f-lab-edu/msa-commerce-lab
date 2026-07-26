package com.msa.commerce.orchestrator.adapter.out.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msa.commerce.orchestrator.adapter.out.product.dto.ProductVerifyApiRequest;
import com.msa.commerce.orchestrator.adapter.out.product.dto.ProductVerifyApiResponse;
import com.msa.commerce.orchestrator.application.port.out.ProductPort;
import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductVerifyAdapter implements ProductPort {

    private static final String VERIFY_PATH = "/api/v1/products/verify";

    private final RestClient productRestClient;

    public ProductVerifyAdapter(@Qualifier("productRestClient") RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    @Override
    public ProductVerification verify(List<ProductVerificationItem> items) {
        try {
            return toDomain(requestVerification(items));
        } catch (RestClientException e) {
            log.error("Product verification request failed for {} item(s)", items.size(), e);
            throw new ProductServiceException("Failed to verify products", e);
        }
    }

    private ProductVerifyApiResponse requestVerification(List<ProductVerificationItem> items) {
        return productRestClient.post()
            .uri(VERIFY_PATH)
            .body(ProductVerifyApiRequest.from(items))
            .retrieve()
            .body(ProductVerifyApiResponse.class);
    }

    // ProductServiceException은 RestClientException이 아니므로 위 catch에 다시 잡히지 않는다.
    private ProductVerification toDomain(ProductVerifyApiResponse response) {
        if (response == null) {
            throw new ProductServiceException("Product service returned an empty verification response");
        }
        return response.toDomain();
    }

}
