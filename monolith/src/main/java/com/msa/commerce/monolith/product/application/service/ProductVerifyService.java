package com.msa.commerce.monolith.product.application.service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVerifyService implements ProductVerifyUseCase {

    private final ProductRepository productRepository;

    private final Random random = new Random();

    @Override
    public ProductVerifyResponse verifyProducts(ProductVerifyCommand command) {
        Map<Long, Product> productMap = retrieveProductMap(command);
        List<ProductVerifyResponse.ProductVerifyResult> results = processVerificationItems(command, productMap);
        Boolean allAvailable = calculateAllAvailable(results);

        return ProductVerifyResponse.builder()
            .allAvailable(allAvailable)
            .results(results)
            .build();
    }

    private Map<Long, Product> retrieveProductMap(ProductVerifyCommand command) {
        List<Long> productIds = command.getItems().stream()
            .map(ProductVerifyCommand.ProductVerifyItem::getProductId)
            .toList();

        return productRepository.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private List<ProductVerifyResponse.ProductVerifyResult> processVerificationItems(ProductVerifyCommand command, Map<Long, Product> productMap) {
        return command.getItems().stream()
            .map(item -> verifyItem(item, productMap))
            .toList();
    }

    private ProductVerifyResponse.ProductVerifyResult verifyItem(ProductVerifyCommand.ProductVerifyItem item, Map<Long, Product> productMap) {
        Product product = productMap.get(item.getProductId());

        if (product == null) {
            return buildUnavailableResult(item.getProductId(), item.getQuantity(), "Product not found");
        }

        return verifyProduct(product, item.getQuantity());
    }

    private Boolean calculateAllAvailable(List<ProductVerifyResponse.ProductVerifyResult> results) {
        return results.stream().allMatch(ProductVerifyResponse.ProductVerifyResult::getAvailable);
    }

    private ProductVerifyResponse.ProductVerifyResult verifyProduct(Product product, Integer requestedQuantity) {
        String unavailableReason = checkProductAvailability(product, requestedQuantity);
        Boolean available = unavailableReason == null;

        // TODO: InventorySnapshots 구현 시 실제 상품별 주문 수량 제한 적용
        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .available(available)
            .status(product.getStatus())
            .requestedQuantity(requestedQuantity)
            .availableStock(1)
            .currentPrice(product.getCurrnectPrice())
            .originalPrice(product.getOriginalPrice())
            .unavailableReason(unavailableReason)
            .build();
    }

    private String checkProductAvailability(Product product, Integer requestedQuantity) {
        if (product.isProductInactive()) {
            return String.format("Product is not active (status: %s )", product.getStatus());
        }

        // TODO: 재고쪽 구현 시 가능 수량 Check 로직 추가
        if (!isStockAvailable()) {
            return "Insufficient stock";
        }

        return product.validateQuantity(requestedQuantity);
    }

    private boolean isStockAvailable() {
        return true;
    }

    private ProductVerifyResponse.ProductVerifyResult buildUnavailableResult(Long productId, Integer requestedQuantity, String reason) {
        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(productId)
            .available(false)
            .requestedQuantity(requestedQuantity)
            .unavailableReason(reason)
            .build();
    }

}

