package com.msa.commerce.monolith.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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
import com.msa.commerce.monolith.user.application.port.in.UserResponse;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateCommand;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUpdateService 테스트")
class UserUpdateServiceTest {

    private static final String CHANGED_FIRST_NAME = "Changed";

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserResponseMapper userResponseMapper = new UserResponseMapper();

    @InjectMocks
    private UserUpdateService userUpdateService;

    @Test
    @DisplayName("전달된 프로필 필드만 갱신된다")
    void updateUserPartially() {
        User user = UserFixture.activeUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userUpdateService.updateUser(UserUpdateCommand.builder()
            .userId(1L)
            .firstName(CHANGED_FIRST_NAME)
            .build());

        assertThat(response.getFirstName()).isEqualTo(CHANGED_FIRST_NAME);
        assertThat(response.getLastName()).isEqualTo("You");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("변경할 필드가 하나도 없으면 NoChangesProvidedException 이 발생한다")
    void updateUserWithoutChanges() {
        assertThatThrownBy(() -> userUpdateService.updateUser(UserUpdateCommand.builder()
            .userId(1L)
            .build()))
            .isInstanceOf(NoChangesProvidedException.class);

        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정하면 ResourceNotFoundException 이 발생한다")
    void updateUserNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userUpdateService.updateUser(UserUpdateCommand.builder()
            .userId(99L)
            .firstName(CHANGED_FIRST_NAME)
            .build()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETED 상태의 사용자는 수정할 수 없다")
    void updateDeletedUser() {
        given(userRepository.findById(1L))
            .willReturn(Optional.of(UserFixture.persistedUser(1L, UserStatus.DELETED, true)));

        assertThatThrownBy(() -> userUpdateService.updateUser(UserUpdateCommand.builder()
            .userId(1L)
            .firstName(CHANGED_FIRST_NAME)
            .build()))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

}
