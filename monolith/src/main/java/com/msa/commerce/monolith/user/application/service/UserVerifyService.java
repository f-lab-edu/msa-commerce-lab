package com.msa.commerce.monolith.user.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.msa.commerce.common.aop.ValidateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyResponse;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyUseCase;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserVerifyService implements UserVerifyUseCase {

    private final UserRepository userRepository;

    @Override
    @ValidateCommand(errorPrefix = "User verification validation failed")
    public UserVerifyResponse verifyUsers(UserVerifyCommand command) {
        Map<Long, User> userMap = retrieveUserMap(command);

        List<UserVerifyResponse.UserVerifyResult> results = command.getUserIds().stream()
            .distinct()
            .map(userId -> verifyUser(userId, userMap))
            .toList();

        return UserVerifyResponse.builder()
            .allValid(results.stream().allMatch(UserVerifyResponse.UserVerifyResult::getValid))
            .results(results)
            .build();
    }

    private Map<Long, User> retrieveUserMap(UserVerifyCommand command) {
        return userRepository.findAllByIds(command.getUserIds()).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private UserVerifyResponse.UserVerifyResult verifyUser(Long userId, Map<Long, User> userMap) {
        User user = userMap.get(userId);

        if (user == null) {
            return UserVerifyResponse.UserVerifyResult.builder()
                .userId(userId)
                .valid(false)
                .invalidReason("User not found")
                .build();
        }

        String invalidReason = checkOrderEligibility(user);

        return UserVerifyResponse.UserVerifyResult.builder()
            .userId(user.getId())
            .userUuid(user.getUserUuid())
            .username(user.getUsername())
            .email(user.getEmail())
            .valid(invalidReason == null)
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .invalidReason(invalidReason)
            .build();
    }

    private String checkOrderEligibility(User user) {
        if (!user.isActive()) {
            return String.format("User is not active (status: %s)", user.getStatus());
        }

        if (!user.isOrderable()) {
            return "Email is not verified";
        }

        return null;
    }

}
