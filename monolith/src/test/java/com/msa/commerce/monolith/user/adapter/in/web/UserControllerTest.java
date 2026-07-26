package com.msa.commerce.monolith.user.adapter.in.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.GlobalExceptionHandler;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.adapter.in.web.mapper.UserMapper;
import com.msa.commerce.monolith.user.application.port.in.UserCreateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserCreateUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserGetUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateUseCase;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyResponse;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyUseCase;
import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.UserStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserCreateUseCase userCreateUseCase;

    @Mock
    private UserGetUseCase userGetUseCase;

    @Mock
    private UserUpdateUseCase userUpdateUseCase;

    @Mock
    private UserLoginPolicyUseCase userLoginPolicyUseCase;

    @Mock
    private UserVerifyUseCase userVerifyUseCase;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserController(userCreateUseCase, userGetUseCase, userUpdateUseCase, userLoginPolicyUseCase,
                    userVerifyUseCase, userMapper))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
            .id(1L)
            .userUuid("018f8c2e-0000-7000-8000-000000000001")
            .username("joel")
            .email("joel@example.com")
            .firstName("Jaeyoung")
            .lastName("You")
            .gender(Gender.MALE)
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .phoneVerified(false)
            .build();
    }

    @Test
    @DisplayName("사용자 생성 API - 201 과 생성된 사용자를 반환한다")
    void createUser() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("joel")
            .email("joel@example.com")
            .password("rawPassword123")
            .firstName("Jaeyoung")
            .lastName("You")
            .build();

        given(userMapper.toCommand(any(UserCreateRequest.class))).willReturn(UserCreateCommand.builder().build());
        given(userCreateUseCase.createUser(any(UserCreateCommand.class))).willReturn(userResponse());

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("joel"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("사용자 생성 API - 필수값이 없으면 400 을 반환한다")
    void createUserWithInvalidRequest() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("")
            .email("not-an-email")
            .password("short")
            .build();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userCreateUseCase, never()).createUser(any(UserCreateCommand.class));
    }

    @Test
    @DisplayName("사용자 조회 API - 200 과 사용자를 반환한다")
    void retrieveUser() throws Exception {
        given(userGetUseCase.getUser(1L)).willReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("joel@example.com"));
    }

    @Test
    @DisplayName("사용자 조회 API - 존재하지 않으면 404 를 반환한다")
    void retrieveUserNotFound() throws Exception {
        given(userGetUseCase.getUser(99L)).willThrow(
            new ResourceNotFoundException("User not found with id: 99", ErrorCode.USER_NOT_FOUND.getCode()));

        mockMvc.perform(get("/api/v1/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("이메일 조회 API - 200 과 사용자를 반환한다")
    void retrieveUserByEmail() throws Exception {
        given(userGetUseCase.getUserByEmail("joel@example.com")).willReturn(userResponse());

        mockMvc.perform(get("/api/v1/users").param("email", "joel@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("joel@example.com"));
    }

    @Test
    @DisplayName("사용자 수정 API - 200 과 수정된 사용자를 반환한다")
    void updateUser() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Changed")
            .build();

        given(userMapper.toUpdateCommand(eq(1L), any(UserUpdateRequest.class)))
            .willReturn(UserUpdateCommand.builder().userId(1L).build());
        given(userUpdateUseCase.updateUser(any(UserUpdateCommand.class))).willReturn(userResponse());

        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("로그인 기록 API - 200 을 반환한다")
    void recordLogin() throws Exception {
        given(userLoginPolicyUseCase.recordLogin(1L)).willReturn(userResponse());

        mockMvc.perform(post("/api/v1/users/1/login-record"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("로그인 정책 수정 API - 200 을 반환한다")
    void updateLoginPolicy() throws Exception {
        UserLoginPolicyRequest request = UserLoginPolicyRequest.builder()
            .status(UserStatus.SUSPENDED)
            .build();

        given(userMapper.toLoginPolicyCommand(eq(1L), any(UserLoginPolicyRequest.class)))
            .willReturn(UserLoginPolicyCommand.builder().userId(1L).status(UserStatus.SUSPENDED).build());
        given(userLoginPolicyUseCase.updateLoginPolicy(any(UserLoginPolicyCommand.class)))
            .willReturn(userResponse());

        mockMvc.perform(patch("/api/v1/users/1/login-policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 검증 API - 검증 결과를 반환한다")
    void verifyUsers() throws Exception {
        UserVerifyRequest request = UserVerifyRequest.builder()
            .userIds(List.of(1L, 99L))
            .build();

        given(userMapper.toVerifyCommand(any(UserVerifyRequest.class)))
            .willReturn(UserVerifyCommand.builder().userIds(List.of(1L, 99L)).build());
        given(userVerifyUseCase.verifyUsers(any(UserVerifyCommand.class))).willReturn(
            UserVerifyResponse.builder()
                .allValid(false)
                .results(List.of(
                    UserVerifyResponse.UserVerifyResult.builder()
                        .userId(1L)
                        .valid(true)
                        .status(UserStatus.ACTIVE)
                        .build(),
                    UserVerifyResponse.UserVerifyResult.builder()
                        .userId(99L)
                        .valid(false)
                        .invalidReason("User not found")
                        .build()
                ))
                .build());

        mockMvc.perform(post("/api/v1/users/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allValid").value(false))
            .andExpect(jsonPath("$.results.length()").value(2))
            .andExpect(jsonPath("$.results[1].invalidReason").value("User not found"));
    }

    @Test
    @DisplayName("사용자 검증 API - userIds 가 비어 있으면 400 을 반환한다")
    void verifyUsersWithEmptyIds() throws Exception {
        UserVerifyRequest request = UserVerifyRequest.builder()
            .userIds(List.of())
            .build();

        mockMvc.perform(post("/api/v1/users/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(userVerifyUseCase, never()).verifyUsers(any(UserVerifyCommand.class));
    }

}
