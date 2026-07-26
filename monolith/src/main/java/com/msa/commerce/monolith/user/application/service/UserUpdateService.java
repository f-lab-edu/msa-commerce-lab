package com.msa.commerce.monolith.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.NoChangesProvidedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateUseCase;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserUpdateService implements UserUpdateUseCase {

    private final UserRepository userRepository;

    private final UserResponseMapper userResponseMapper;

    @Override
    @ValidateCommand(errorPrefix = "User update validation failed")
    public UserResponse updateUser(UserUpdateCommand command) {
        if (command.hasNoChanges()) {
            throw new NoChangesProvidedException(
                "No fields to update provided.",
                ErrorCode.USER_NO_CHANGES_PROVIDED.getCode()
            );
        }

        User user = userRepository.findById(command.getUserId())
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with id: " + command.getUserId(),
                ErrorCode.USER_NOT_FOUND.getCode()
            ));

        user.updateProfile(
            command.getFirstName(),
            command.getLastName(),
            command.getPhoneNumber(),
            command.getDateOfBirth(),
            command.getGender(),
            command.getProfileImageUrl()
        );

        return userResponseMapper.toResponse(userRepository.save(user));
    }

}
