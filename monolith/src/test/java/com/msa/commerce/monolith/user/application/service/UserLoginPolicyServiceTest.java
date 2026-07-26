package com.msa.commerce.monolith.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.NoChangesProvidedException;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserLoginPolicyService 테스트")
class UserLoginPolicyServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserResponseMapper userResponseMapper = new UserResponseMapper();

    @InjectMocks
    private UserLoginPolicyService userLoginPolicyService;

    @Test
    @DisplayName("로그인을 기록하면 lastLoginAt 이 채워진다")
    void recordLogin() {
        given(userRepository.findById(1L)).willReturn(Optional.of(UserFixture.activeUser(1L)));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userLoginPolicyService.recordLogin(1L);

        assertThat(response.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 로그인은 기록할 수 없다")
    void recordLoginForUnknownUser() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userLoginPolicyService.recordLogin(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETED 사용자의 로그인은 기록할 수 없다")
    void recordLoginForDeletedUser() {
        given(userRepository.findById(1L))
            .willReturn(Optional.of(UserFixture.persistedUser(1L, UserStatus.DELETED, true)));

        assertThatThrownBy(() -> userLoginPolicyService.recordLogin(1L))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("계정 상태를 SUSPENDED 로 변경한다")
    void updateStatus() {
        given(userRepository.findById(1L)).willReturn(Optional.of(UserFixture.activeUser(1L)));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userLoginPolicyService.updateLoginPolicy(UserLoginPolicyCommand.builder()
            .userId(1L)
            .status(UserStatus.SUSPENDED)
            .build());

        assertThat(response.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(response.getEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 여부만 변경할 수 있다")
    void updateEmailVerifiedOnly() {
        given(userRepository.findById(1L))
            .willReturn(Optional.of(UserFixture.persistedUser(1L, UserStatus.ACTIVE, false)));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userLoginPolicyService.updateLoginPolicy(UserLoginPolicyCommand.builder()
            .userId(1L)
            .emailVerified(true)
            .build());

        assertThat(response.getEmailVerified()).isTrue();
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("DELETED 사용자도 상태를 복구할 수 있다")
    void restoreDeletedUser() {
        given(userRepository.findById(1L))
            .willReturn(Optional.of(UserFixture.persistedUser(1L, UserStatus.DELETED, true)));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userLoginPolicyService.updateLoginPolicy(UserLoginPolicyCommand.builder()
            .userId(1L)
            .status(UserStatus.ACTIVE)
            .build());

        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("변경할 정책이 하나도 없으면 NoChangesProvidedException 이 발생한다")
    void updateWithoutChanges() {
        assertThatThrownBy(() -> userLoginPolicyService.updateLoginPolicy(UserLoginPolicyCommand.builder()
            .userId(1L)
            .build()))
            .isInstanceOf(NoChangesProvidedException.class);

        verify(userRepository, never()).findById(anyLong());
    }

}
