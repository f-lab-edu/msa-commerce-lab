package com.msa.commerce.orchestrator.application.port.out;

import java.util.List;

import com.msa.commerce.orchestrator.domain.vo.ProductVerification;
import com.msa.commerce.orchestrator.domain.vo.ProductVerificationItem;

public interface ProductPort {

    ProductVerification verify(List<ProductVerificationItem> items);

}
