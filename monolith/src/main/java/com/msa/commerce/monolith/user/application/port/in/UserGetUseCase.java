package com.msa.commerce.monolith.user.application.port.in;

public interface UserGetUseCase {

    UserResponse getUser(Long userId);

    UserResponse getUserByEmail(String email);

}
