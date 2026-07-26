package com.msa.commerce.monolith.user.application.port.in;

public interface UserCreateUseCase {

    UserResponse createUser(UserCreateCommand command);

}
