package com.msa.commerce.monolith.user.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msa.commerce.monolith.user.adapter.in.web.mapper.UserMapper;
import com.msa.commerce.monolith.user.application.port.in.UserCreateUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserGetUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyResponse;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserCreateUseCase userCreateUseCase;

    private final UserGetUseCase userGetUseCase;

    private final UserUpdateUseCase userUpdateUseCase;

    private final UserLoginPolicyUseCase userLoginPolicyUseCase;

    private final UserVerifyUseCase userVerifyUseCase;

    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userCreateUseCase.createUser(userMapper.toCommand(request)));
    }

    @PostMapping("/verify")
    public ResponseEntity<UserVerifyResponse> verifyUsers(@Valid @RequestBody UserVerifyRequest request) {
        return ResponseEntity.ok(userVerifyUseCase.verifyUsers(userMapper.toVerifyCommand(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> retrieveUser(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userGetUseCase.getUser(userId));
    }

    @GetMapping
    public ResponseEntity<UserResponse> retrieveUserByEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(userGetUseCase.getUserByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long userId,
        @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userUpdateUseCase.updateUser(userMapper.toUpdateCommand(userId, request)));
    }

    @PostMapping("/{id}/login-record")
    public ResponseEntity<UserResponse> recordLogin(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userLoginPolicyUseCase.recordLogin(userId));
    }

    @PatchMapping("/{id}/login-policy")
    public ResponseEntity<UserResponse> updateLoginPolicy(@PathVariable("id") Long userId,
        @Valid @RequestBody UserLoginPolicyRequest request) {
        return ResponseEntity.ok(
            userLoginPolicyUseCase.updateLoginPolicy(userMapper.toLoginPolicyCommand(userId, request)));
    }

}
