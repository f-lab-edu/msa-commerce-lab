package com.msa.commerce.monolith.product.application.service;

import java.math.BigDecimal;
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
import com.msa.commerce.monolith.product.domain.ProductStatus;

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

    private List<ProductVerifyResponse.ProductVerifyResult> processVerificationItems(
        ProductVerifyCommand command, Map<Long, Product> productMap) {
        return command.getItems().stream()
            .map(item -> verifyItem(item, productMap))
            .toList();
    }

    private ProductVerifyResponse.ProductVerifyResult verifyItem(
        ProductVerifyCommand.ProductVerifyItem item, Map<Long, Product> productMap) {
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
        
        PriceInfo priceInfo = calculatePriceInfo(product);
        OrderLimits orderLimits = getOrderLimits();

        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .available(available)
            .status(product.getStatus())
            .requestedQuantity(requestedQuantity)
            .availableStock(1)
            .currentPrice(priceInfo.currentPrice())
            .originalPrice(priceInfo.originalPrice())
            .priceChanged(priceInfo.priceChanged())
            .unavailableReason(unavailableReason)
            .minOrderQuantity(orderLimits.minQuantity())
            .maxOrderQuantity(orderLimits.maxQuantity())
            .build();
    }

    private String checkProductAvailability(Product product, Integer requestedQuantity) {
        String statusReason = checkProductStatus(product.getStatus());
        if (statusReason != null) {
            return statusReason;
        }

        // TODO: 재고쪽 구현 시 가능 수량 Check 로직 추가
        String stockReason = checkStockAvailability();
        if (stockReason != null) {
            return stockReason;
        }

        return checkQuantityLimits(requestedQuantity);
    }

    private String checkProductStatus(ProductStatus status) {
        return switch (status) {
            case ACTIVE -> null;
            case INACTIVE -> "Product is not active (status: INACTIVE)";
            case DRAFT -> "Product is still in draft status";
            case ARCHIVED -> "Product is archived";
        };
    }

    private String checkStockAvailability() {
        return "Insufficient stock";
    }

    private String checkQuantityLimits(Integer requestedQuantity) {
        OrderLimits limits = getOrderLimits();
        
        if (requestedQuantity < limits.minQuantity()) {
            return "Quantity below minimum order quantity";
        }
        
        if (requestedQuantity > limits.maxQuantity()) {
            return "Quantity exceeds maximum order quantity";
        }
        
        return null;
    }

    private PriceInfo calculatePriceInfo(Product product) {
        Boolean priceChanged = random.nextDouble() < 0.1;
        BigDecimal currentPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        BigDecimal originalPrice = product.getBasePrice();
        
        return new PriceInfo(currentPrice, originalPrice, priceChanged);
    }

    private OrderLimits getOrderLimits() {
        return new OrderLimits(1, 100);
    }

    private record PriceInfo(BigDecimal currentPrice, BigDecimal originalPrice, Boolean priceChanged) {}
    
    private record OrderLimits(Integer minQuantity, Integer maxQuantity) {}

    private ProductVerifyResponse.ProductVerifyResult buildUnavailableResult(Long productId, Integer requestedQuantity, String reason) {
        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(productId)
            .available(false)
            .requestedQuantity(requestedQuantity)
            .unavailableReason(reason)
            .build();
    }
}