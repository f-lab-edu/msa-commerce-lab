package com.msa.commerce.monolith.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.common.exception.DuplicateResourceException;
import com.msa.commerce.common.exception.ErrorCode;
import com.msa.commerce.monolith.user.application.port.in.UserCreateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.out.PasswordEncryptor;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCreateService 테스트")
class UserCreateServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @Spy
    private UserResponseMapper userResponseMapper = new UserResponseMapper();

    @InjectMocks
    private UserCreateService userCreateService;

    private UserCreateCommand command() {
        return UserCreateCommand.builder()
            .username("joel")
            .email("joel@example.com")
            .password("rawPassword123")
            .firstName("Jaeyoung")
            .lastName("You")
            .phoneNumber("010-1234-5678")
            .dateOfBirth(UserFixture.DATE_OF_BIRTH)
            .gender(Gender.MALE)
            .build();
    }

    @Test
    @DisplayName("사용자를 생성하면 ACTIVE 상태로 저장되고 응답이 반환된다")
    void createUser() {
        given(passwordEncryptor.encrypt("rawPassword123")).willReturn("$2a$10$hashed");
        given(userRepository.existsByUsername("joel")).willReturn(false);
        given(userRepository.existsByEmail("joel@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(UserFixture.activeUser(1L));

        UserResponse response = userCreateService.createUser(command());

        assertThat(response).isNotNull()
            .extracting(UserResponse::getId, UserResponse::getStatus)
            .containsExactly(1L, UserStatus.ACTIVE);
        assertThat(response).extracting(UserResponse::getUserUuid).asString().isNotBlank();
    }

    @Test
    @DisplayName("평문 비밀번호는 해싱되어 저장된다")
    void createUserEncryptsPassword() {
        given(passwordEncryptor.encrypt("rawPassword123")).willReturn("$2a$10$hashed");
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(UserFixture.activeUser(1L));

        userCreateService.createUser(command());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("rawPassword123");
    }

    @Test
    @DisplayName("응답에는 비밀번호 관련 정보가 포함되지 않는다")
    void responseDoesNotExposePassword() {
        given(passwordEncryptor.encrypt(anyString())).willReturn("$2a$10$hashed");
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(UserFixture.activeUser(1L));

        UserResponse response = userCreateService.createUser(command());

        assertThat(response).isNotNull()
            .extracting("username", "email").doesNotContainNull();
        assertThat(UserResponse.class.getDeclaredFields())
            .noneMatch(field -> field.getName().toLowerCase().contains("password"));
    }

    @Test
    @DisplayName("username 이 중복되면 DuplicateResourceException 이 발생한다")
    void createUserWithDuplicateUsername() {
        given(userRepository.existsByUsername("joel")).willReturn(true);

        assertThatThrownBy(() -> userCreateService.createUser(command()))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Username already exists")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_USERNAME_DUPLICATE.getCode());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("email 이 중복되면 DuplicateResourceException 이 발생한다")
    void createUserWithDuplicateEmail() {
        given(userRepository.existsByUsername("joel")).willReturn(false);
        given(userRepository.existsByEmail("joel@example.com")).willReturn(true);

        assertThatThrownBy(() -> userCreateService.createUser(command()))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email already exists")
            .extracting("errorCode")
            .isEqualTo(ErrorCode.USER_EMAIL_DUPLICATE.getCode());

        verify(userRepository, never()).save(any(User.class));
    }

}
