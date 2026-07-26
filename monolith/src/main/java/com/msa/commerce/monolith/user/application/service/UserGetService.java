package com.msa.commerce.monolith.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.application.port.in.UserGetUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGetService implements UserGetUseCase {

    private final UserRepository userRepository;

    private final UserResponseMapper userResponseMapper;

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> userNotFound("id", String.valueOf(userId)));

        return userResponseMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> userNotFound("email", email));

        return userResponseMapper.toResponse(user);
    }

    private ResourceNotFoundException userNotFound(String field, String value) {
        return new ResourceNotFoundException(
            String.format("User not found with %s: %s", field, value),
            ErrorCode.USER_NOT_FOUND.getCode()
        );
    }

}
