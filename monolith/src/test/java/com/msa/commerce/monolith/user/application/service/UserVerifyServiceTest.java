package com.msa.commerce.monolith.user.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msa.commerce.monolith.user.application.port.in.UserVerifyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyResponse;
import com.msa.commerce.monolith.user.application.port.out.UserRepository;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserVerifyService 테스트")
class UserVerifyServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserVerifyService userVerifyService;

    private UserVerifyCommand command(Long... userIds) {
        return UserVerifyCommand.builder().userIds(List.of(userIds)).build();
    }

    @Test
    @DisplayName("모든 사용자가 주문 가능하면 allValid 는 true 이다")
    void verifyAllValid() {
        given(userRepository.findAllByIds(List.of(1L, 2L)))
            .willReturn(List.of(UserFixture.activeUser(1L), UserFixture.activeUser(2L)));

        UserVerifyResponse response = userVerifyService.verifyUsers(command(1L, 2L));

        assertThat(response.getAllValid()).isTrue();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults()).allMatch(UserVerifyResponse.UserVerifyResult::getValid);
        assertThat(response.getResults().get(0).getUsername()).isEqualTo("joel1");
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 유효하지 않게 표시된다")
    void verifyMissingUser() {
        given(userRepository.findAllByIds(List.of(1L, 99L)))
            .willReturn(List.of(UserFixture.activeUser(1L)));

        UserVerifyResponse response = userVerifyService.verifyUsers(command(1L, 99L));

        assertThat(response.getAllValid()).isFalse();
        assertThat(response.getResults()).hasSize(2);

        UserVerifyResponse.UserVerifyResult missing = response.getResults().get(1);
        assertThat(missing.getUserId()).isEqualTo(99L);
        assertThat(missing.getValid()).isFalse();
        assertThat(missing.getInvalidReason()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("비활성 사용자는 상태와 함께 사유가 반환된다")
    void verifyInactiveUser() {
        given(userRepository.findAllByIds(List.of(1L)))
            .willReturn(List.of(UserFixture.persistedUser(1L, UserStatus.SUSPENDED, true)));

        UserVerifyResponse response = userVerifyService.verifyUsers(command(1L));

        assertThat(response.getAllValid()).isFalse();
        assertThat(response.getResults().get(0).getInvalidReason())
            .isEqualTo("User is not active (status: SUSPENDED)");
    }

    @Test
    @DisplayName("이메일 미인증 사용자는 주문할 수 없다")
    void verifyEmailNotVerifiedUser() {
        given(userRepository.findAllByIds(List.of(1L)))
            .willReturn(List.of(UserFixture.persistedUser(1L, UserStatus.ACTIVE, false)));

        UserVerifyResponse response = userVerifyService.verifyUsers(command(1L));

        assertThat(response.getAllValid()).isFalse();
        assertThat(response.getResults().get(0).getInvalidReason()).isEqualTo("Email is not verified");
        assertThat(response.getResults().get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("중복된 사용자 ID 는 한 번만 검증된다")
    void verifyDuplicatedIds() {
        given(userRepository.findAllByIds(List.of(1L, 1L)))
            .willReturn(List.of(UserFixture.activeUser(1L)));

        UserVerifyResponse response = userVerifyService.verifyUsers(command(1L, 1L));

        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getAllValid()).isTrue();
    }

}
