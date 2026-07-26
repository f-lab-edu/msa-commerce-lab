package com.msa.commerce.monolith.user.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.UserStatus;

import lombok.Builder;
import lombok.Getter;

// 비밀번호 해시는 어떤 경우에도 응답에 포함하지 않는다.
@Getter
@Builder
public class UserResponse {

    private final Long id;

    private final String userUuid;

    private final String username;

    private final String email;

    private final String firstName;

    private final String lastName;

    private final String phoneNumber;

    private final LocalDate dateOfBirth;

    private final Gender gender;

    private final UserStatus status;

    private final Boolean emailVerified;

    private final Boolean phoneVerified;

    private final String profileImageUrl;

    private final LocalDateTime lastLoginAt;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

}
