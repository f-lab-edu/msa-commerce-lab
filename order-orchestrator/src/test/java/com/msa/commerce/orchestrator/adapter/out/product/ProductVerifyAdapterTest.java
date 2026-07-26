package com.msa.commerce.orchestrator.adapter.out.product;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;
import com.msa.commerce.orchestrator.domain.vo.VerifiedProduct;

@DisplayName("ProductVerifyAdapter 단위 테스트")
class ProductVerifyAdapterTest {

    private static final String VERIFY_URL = "http://product-service/api/v1/products/verify";

    private MockRestServiceServer server;

    private ProductVerifyAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://product-service");
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new ProductVerifyAdapter(builder.build());
    }

    @Test
    @DisplayName("검증 응답을 도메인 객체로 변환한다")
    void verify_MapsResponseToDomain() {
        server.expect(requestTo(VERIFY_URL))
            .andExpect(method(org.springframework.http.HttpMethod.POST))
            .andExpect(jsonPath("$.items[0].productId").value(101))
            .andExpect(jsonPath("$.items[0].quantity").value(2))
            .andRespond(withSuccess("""
                {
                  "allAvailable": true,
                  "results": [
                    {
                      "productId": 101,
                      "sku": "SKU-101",
                      "name": "무선 키보드",
                      "available": true,
                      "availableStock": 50,
                      "currentPrice": 25000,
                      "unavailableReason": null
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        ProductVerification verification = adapter.verify(List.of(new ProductVerificationItem(101L, 2)));

        assertThat(verification.allAvailable()).isTrue();
        VerifiedProduct product = verification.find(101L);
        assertThat(product.name()).isEqualTo("무선 키보드");
        assertThat(product.sku()).isEqualTo("SKU-101");
        assertThat(product.currentPrice()).isEqualByComparingTo("25000");
        server.verify();
    }

    @Test
    @DisplayName("구매 불가 상품은 사유와 함께 매핑된다")
    void verify_MapsUnavailableProduct() {
        server.expect(requestTo(VERIFY_URL))
            .andRespond(withSuccess("""
                {
                  "allAvailable": false,
                  "results": [
                    {
                      "productId": 101,
                      "sku": "SKU-101",
                      "name": "무선 키보드",
                      "available": false,
                      "availableStock": 0,
                      "currentPrice": 25000,
                      "unavailableReason": "재고 부족"
                    }
                  ]
                }
                """, MediaType.APPLICATION_JSON));

        ProductVerification verification = adapter.verify(List.of(new ProductVerificationItem(101L, 2)));

        assertThat(verification.allAvailable()).isFalse();
        assertThat(verification.unavailableProducts())
            .singleElement()
            .satisfies(product -> assertThat(product.unavailableReason()).isEqualTo("재고 부족"));
    }

    @Test
    @DisplayName("Product Service 장애 시 ProductServiceException으로 전환된다")
    void verify_WrapsServerError() {
        server.expect(requestTo(VERIFY_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> adapter.verify(List.of(new ProductVerificationItem(101L, 2))))
            .isInstanceOf(ProductServiceException.class)
            .hasMessageContaining("Failed to verify products");
    }

    @Test
    @DisplayName("응답 본문이 비어 있으면 ProductServiceException을 던진다")
    void verify_ThrowsOnEmptyBody() {
        server.expect(requestTo(VERIFY_URL)).andRespond(withSuccess());

        assertThatThrownBy(() -> adapter.verify(List.of(new ProductVerificationItem(101L, 2))))
            .isInstanceOf(ProductServiceException.class)
            .hasMessageContaining("empty verification response");
    }

}
