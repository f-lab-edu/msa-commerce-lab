package com.msa.commerce.monolith.user.application.port.in;

public interface UserVerifyUseCase {

    UserVerifyResponse verifyUsers(UserVerifyCommand command);

}
