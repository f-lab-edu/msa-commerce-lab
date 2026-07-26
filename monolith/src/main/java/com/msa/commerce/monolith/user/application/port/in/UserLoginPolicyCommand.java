package com.msa.commerce.monolith.user.application.port.in;

import com.msa.commerce.monolith.user.domain.UserStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginPolicyCommand {

    @NotNull(message = "User ID is required.")
    @Positive(message = "User ID must be positive.")
    private final Long userId;

    private final UserStatus status;

    private final Boolean emailVerified;

    private final Boolean phoneVerified;

    public boolean hasNoChanges() {
        return status == null && emailVerified == null && phoneVerified == null;
    }

}
