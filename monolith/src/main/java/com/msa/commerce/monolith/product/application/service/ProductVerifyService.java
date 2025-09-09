package com.msa.commerce.monolith.product.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.monolith.product.application.port.in.ProductVerifyCommand;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyResponse;
import com.msa.commerce.monolith.product.application.port.in.ProductVerifyUseCase;
import com.msa.commerce.monolith.product.application.port.out.ProductRepository;
import com.msa.commerce.monolith.product.domain.Product;
import com.msa.commerce.monolith.product.domain.ProductStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVerifyService implements ProductVerifyUseCase {

    private final ProductRepository productRepository;

    @Override
    public ProductVerifyResponse verifyProducts(ProductVerifyCommand command) {
        List<Long> productIds = command.getItems().stream()
            .map(ProductVerifyCommand.ProductVerifyItem::getProductId)
            .toList();

        Map<Long, Product> productMap = productRepository.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<ProductVerifyResponse.ProductVerifyResult> results = new ArrayList<>();
        boolean allAvailable = true;

        for (ProductVerifyCommand.ProductVerifyItem item : command.getItems()) {
            Product product = productMap.get(item.getProductId());

            if (product == null) {
                results.add(buildUnavailableResult(item.getProductId(), item.getQuantity(), "Product not found"));
                allAvailable = false;

            } else {
                ProductVerifyResponse.ProductVerifyResult result = verifyProduct(product, item.getQuantity());
                results.add(result);
                if (!result.isAvailable()) {
                    allAvailable = false;
                }
            }
        }

        return ProductVerifyResponse.builder()
            .allAvailable(allAvailable)
            .results(results)
            .build();
    }

    private ProductVerifyResponse.ProductVerifyResult verifyProduct(Product product, Integer requestedQuantity) {
        boolean available = true;
        String unavailableReason = null;

        // Check product status
        if (product.getStatus() != ProductStatus.ACTIVE) {
            available = false;
            unavailableReason = String.format("Product is not active (status: %s)", product.getStatus());
        }

        // TODO: 재고쪽 구현 시 가능 수량 Check 로직 추가
        if (available) {
            available = false;
            unavailableReason = "Insufficient stock";
        }

        BigDecimal currentPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        BigDecimal originalPrice = product.getBasePrice();

        // TODO: InventorySnapshots 구현 시 실제 상품별 주문 수량 제한 적용

        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .available(available)
            .status(product.getStatus())
            .requestedQuantity(requestedQuantity)
            .availableStock(1)
            .currentPrice(currentPrice)
            .originalPrice(originalPrice)
            .unavailableReason(unavailableReason)
            .build();
    }

    private ProductVerifyResponse.ProductVerifyResult buildUnavailableResult(Long productId, Integer requestedQuantity,
        String reason) {
        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(productId)
            .available(false)
            .requestedQuantity(requestedQuantity)
            .unavailableReason(reason)
            .build();
    }

}
