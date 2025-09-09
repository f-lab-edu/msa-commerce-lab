package com.msa.commerce.monolith.product.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        List<Long> productIds = command.getItems().stream()
            .map(ProductVerifyCommand.ProductVerifyItem::getProductId)
            .collect(Collectors.toList());
        
        Map<Long, Product> productMap = productRepository.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        
        List<ProductVerifyResponse.ProductVerifyResult> results = new ArrayList<>();
        boolean allAvailable = true;
        
        for (ProductVerifyCommand.ProductVerifyItem item : command.getItems()) {
            Product product = productMap.get(item.getProductId());
            
            if (product == null) {
                results.add(buildUnavailableResult(item.getProductId(), item.getQuantity(), 
                    "Product not found"));
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
            unavailableReason = "Product is not active (status: " + product.getStatus() + ")";
        }
        
        // Simulate stock check (random stock between 0 and 100)
        Integer availableStock = simulateAvailableStock();
        if (available && availableStock < requestedQuantity) {
            available = false;
            unavailableReason = "Insufficient stock";
        }
        
        // Simulate price change detection (10% chance)
        boolean priceChanged = random.nextDouble() < 0.1;
        BigDecimal currentPrice = product.getSalePrice() != null ? product.getSalePrice() : product.getBasePrice();
        BigDecimal originalPrice = product.getBasePrice();
        
        // Simulate order quantity limits
        Integer minOrderQuantity = 1;
        Integer maxOrderQuantity = 100;
        
        if (available && requestedQuantity < minOrderQuantity) {
            available = false;
            unavailableReason = "Quantity below minimum order quantity";
        }
        
        if (available && requestedQuantity > maxOrderQuantity) {
            available = false;
            unavailableReason = "Quantity exceeds maximum order quantity";
        }
        
        return ProductVerifyResponse.ProductVerifyResult.builder()
            .productId(product.getId())
            .sku(product.getSku())
            .name(product.getName())
            .available(available)
            .status(product.getStatus())
            .requestedQuantity(requestedQuantity)
            .availableStock(availableStock)
            .currentPrice(currentPrice)
            .originalPrice(originalPrice)
            .priceChanged(priceChanged)
            .unavailableReason(unavailableReason)
            .minOrderQuantity(minOrderQuantity)
            .maxOrderQuantity(maxOrderQuantity)
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
    
    private Integer simulateAvailableStock() {
        // Simulate stock (random value between 0 and 100)
        // In real implementation, this would query an inventory service or database
        return random.nextInt(101);
    }
}