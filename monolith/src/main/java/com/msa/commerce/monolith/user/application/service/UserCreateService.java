package com.msa.commerce.monolith.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.user.application.port.in.UserCreateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserCreateUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.PasswordEncryptor;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCreateService implements UserCreateUseCase {

    private final UserRepository userRepository;

    private final PasswordEncryptor passwordEncryptor;

    private final UserResponseMapper userResponseMapper;

    @Override
    @ValidateCommand(errorPrefix = "User creation validation failed")
    public UserResponse createUser(UserCreateCommand command) {
        validateDuplicateUsername(command.getUsername());
        validateDuplicateEmail(command.getEmail());

        User user = User.builder()
            .username(command.getUsername())
            .email(command.getEmail())
            .passwordHash(passwordEncryptor.encrypt(command.getPassword()))
            .firstName(command.getFirstName())
            .lastName(command.getLastName())
            .phoneNumber(command.getPhoneNumber())
            .dateOfBirth(command.getDateOfBirth())
            .gender(command.getGender())
            .profileImageUrl(command.getProfileImageUrl())
            .build();

        return userResponseMapper.toResponse(userRepository.save(user));
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException(
                "Username already exists: " + username,
                ErrorCode.USER_USERNAME_DUPLICATE.getCode()
            );
        }
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                "Email already exists: " + email,
                ErrorCode.USER_EMAIL_DUPLICATE.getCode()
            );
        }
    }

}
