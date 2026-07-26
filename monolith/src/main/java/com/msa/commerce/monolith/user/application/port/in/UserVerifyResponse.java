package com.msa.commerce.monolith.user.application.port.in;

import java.util.List;

import com.msa.commerce.monolith.user.domain.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerifyResponse {

    private Boolean allValid;

    private List<UserVerifyResult> results;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserVerifyResult {

        private Long userId;

        private String userUuid;

        private String username;

        private String email;

        private Boolean valid;

        private UserStatus status;

        private Boolean emailVerified;

        private String invalidReason;

    }

}
