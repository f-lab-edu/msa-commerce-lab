package com.msa.commerce.payment.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPaymentRequest(

    @NotBlank(message = "Cancel reason is required")
    @Size(max = 500, message = "Cancel reason cannot exceed 500 characters")
    String reason
) {

}
