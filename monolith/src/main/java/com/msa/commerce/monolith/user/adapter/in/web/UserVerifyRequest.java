package com.msa.commerce.monolith.user.adapter.in.web;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerifyRequest {

    @NotEmpty(message = "User IDs cannot be empty.")
    private List<@Positive(message = "User ID must be positive.") Long> userIds;

}
