package com.msa.commerce.monolith.user.application.port.in;

public interface UserUpdateUseCase {

    UserResponse updateUser(UserUpdateCommand command);

}
