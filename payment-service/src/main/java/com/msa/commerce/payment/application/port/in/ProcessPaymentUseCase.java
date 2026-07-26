package com.msa.commerce.payment.application.port.in;

import com.msa.commerce.payment.application.port.in.command.ProcessPaymentCommand;
import com.msa.commerce.payment.application.port.in.response.PaymentResponse;

public interface ProcessPaymentUseCase {

    PaymentResponse process(ProcessPaymentCommand command);

}
