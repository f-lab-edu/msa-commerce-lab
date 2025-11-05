package com.msa.commerce.orchestrator.application.port.out;

import com.msa.commerce.orchestrator.domain.vo.ProductInfo;

public interface ProductPort {

    ProductInfo getProductInfo(Long productId);

}
