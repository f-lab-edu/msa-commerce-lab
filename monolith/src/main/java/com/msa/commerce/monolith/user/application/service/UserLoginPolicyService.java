package com.msa.commerce.monolith.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.NoChangesProvidedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLoginPolicyService implements UserLoginPolicyUseCase {

    private final UserRepository userRepository;

    private final UserResponseMapper userResponseMapper;

    @Override
    public UserResponse recordLogin(Long userId) {
        User user = userRepository.findById(userId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> userNotFound(userId));

        user.recordLogin();

        return userResponseMapper.toResponse(userRepository.save(user));
    }

    @Override
    @ValidateCommand(errorPrefix = "User login policy validation failed")
    public UserResponse updateLoginPolicy(UserLoginPolicyCommand command) {
        if (command.hasNoChanges()) {
            throw new NoChangesProvidedException(
                "No login policy fields to update provided.",
                ErrorCode.USER_NO_CHANGES_PROVIDED.getCode()
            );
        }

        // 상태 복구(DELETED -> ACTIVE)도 Auth Service 의 정책 관리 범위이므로 삭제 여부로 걸러내지 않는다.
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> userNotFound(command.getUserId()));

        user.updateLoginPolicy(command.getStatus(), command.getEmailVerified(), command.getPhoneVerified());

        return userResponseMapper.toResponse(userRepository.save(user));
    }

    private ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException(
            "User not found with id: " + userId,
            ErrorCode.USER_NOT_FOUND.getCode()
        );
    }

}
