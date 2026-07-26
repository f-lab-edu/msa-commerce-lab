package com.msa.commerce.monolith.user.application.port.in;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerifyCommand {

    @NotEmpty(message = "User IDs cannot be empty.")
    private List<Long> userIds;

}
