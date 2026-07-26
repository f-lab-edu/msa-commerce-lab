package com.msa.commerce.monolith.user.adapter.in.web;

import com.msa.commerce.monolith.user.domain.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Auth Service 전용 로그인 정책 갱신 요청.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginPolicyRequest {

    private UserStatus status;

    private Boolean emailVerified;

    private Boolean phoneVerified;

}
