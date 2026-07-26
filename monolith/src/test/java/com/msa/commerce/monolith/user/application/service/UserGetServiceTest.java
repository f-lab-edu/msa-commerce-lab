package com.msa.commerce.monolith.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.common.exception.ResourceNotFoundException;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserGetService 테스트")
class UserGetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserResponseMapper userResponseMapper = new UserResponseMapper();

    @InjectMocks
    private UserGetService userGetService;

    @Test
    @DisplayName("ID 로 사용자를 조회한다")
    void getUserById() {
        given(userRepository.findById(1L)).willReturn(Optional.of(UserFixture.activeUser(1L)));

        UserResponse response = userGetService.getUser(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("joel1");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 ResourceNotFoundException 이 발생한다")
    void getUserNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userGetService.getUser(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with id: 99")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("DELETED 상태의 사용자는 조회되지 않는다")
    void getDeletedUser() {
        given(userRepository.findById(1L))
            .willReturn(Optional.of(UserFixture.persistedUser(1L, UserStatus.DELETED, true)));

        assertThatThrownBy(() -> userGetService.getUser(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회한다")
    void getUserByEmail() {
        given(userRepository.findByEmail("joel1@example.com"))
            .willReturn(Optional.of(UserFixture.activeUser(1L)));

        UserResponse response = userGetService.getUserByEmail("joel1@example.com");

        assertThat(response.getEmail()).isEqualTo("joel1@example.com");
    }

    @Test
    @DisplayName("이메일로 조회한 사용자가 없으면 ResourceNotFoundException 이 발생한다")
    void getUserByEmailNotFound() {
        given(userRepository.findByEmail("none@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userGetService.getUserByEmail("none@example.com"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("none@example.com");
    }

}
