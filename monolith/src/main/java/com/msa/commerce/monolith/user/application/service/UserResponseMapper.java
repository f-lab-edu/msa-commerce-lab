package com.msa.commerce.monolith.user.application.service;

import org.springframework.stereotype.Component;

import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.domain.User;

@Component
public class UserResponseMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
            .id(user.getId())
            .userUuid(user.getUserUuid())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender())
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .phoneVerified(user.getPhoneVerified())
            .profileImageUrl(user.getProfileImageUrl())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

}
