package com.msa.commerce.monolith.user.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.msa.commerce.monolith.user.fixture.UserFixture;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Nested
    @DisplayName("사용자 생성")
    class Create {

        @Test
        @DisplayName("생성 시 UUID 가 발급되고 기본 상태는 ACTIVE, 인증 여부는 false 이다")
        void createUserWithDefaults() {
            User user = UserFixture.newUser();

            assertThat(user.getUserUuid()).isNotBlank();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getEmailVerified()).isFalse();
            assertThat(user.getPhoneVerified()).isFalse();
            assertThat(user.getLastLoginAt()).isNull();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성할 때마다 서로 다른 UUID 가 발급된다")
        void createUserWithUniqueUuid() {
            User first = UserFixture.newUser();
            User second = UserFixture.newUser();

            assertThat(first.getUserUuid()).isNotEqualTo(second.getUserUuid());
        }

        @Test
        @DisplayName("username 이 비어 있으면 예외가 발생한다")
        void createUserWithoutUsername() {
            assertThatThrownBy(() -> UserFixture.defaultBuilder().username(" ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is required.");
        }

        @Test
        @DisplayName("email 이 없으면 예외가 발생한다")
        void createUserWithoutEmail() {
            assertThatThrownBy(() -> UserFixture.defaultBuilder().email(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required.");
        }

        @Test
        @DisplayName("비밀번호 해시가 없으면 예외가 발생한다")
        void createUserWithoutPassword() {
            assertThatThrownBy(() -> UserFixture.defaultBuilder().passwordHash(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required.");
        }

        @Test
        @DisplayName("이름이 없으면 예외가 발생한다")
        void createUserWithoutName() {
            assertThatThrownBy(() -> UserFixture.defaultBuilder().firstName(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name is required.");

            assertThatThrownBy(() -> UserFixture.defaultBuilder().lastName(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name is required.");
        }

        @Test
        @DisplayName("username 이 50자를 넘으면 예외가 발생한다")
        void createUserWithTooLongUsername() {
            assertThatThrownBy(() -> UserFixture.defaultBuilder().username("a".repeat(51)).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot exceed 50 characters.");
        }

    }

    @Nested
    @DisplayName("프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("null 이 아닌 필드만 부분 갱신된다")
        void updateProfilePartially() {
            User user = UserFixture.activeUser(1L);

            user.updateProfile("Changed", null, null, null, null, null);

            assertThat(user.getFirstName()).isEqualTo("Changed");
            assertThat(user.getLastName()).isEqualTo("You");
            assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
            assertThat(user.getDateOfBirth()).isEqualTo(UserFixture.DATE_OF_BIRTH);
        }

        @Test
        @DisplayName("모든 프로필 필드를 갱신할 수 있다")
        void updateAllProfileFields() {
            User user = UserFixture.activeUser(1L);
            LocalDate newBirthDate = LocalDate.of(2000, 12, 25);

            user.updateProfile("New", "Name", "010-0000-0000", newBirthDate, Gender.FEMALE,
                "https://example.com/new.png");

            assertThat(user.getFirstName()).isEqualTo("New");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getPhoneNumber()).isEqualTo("010-0000-0000");
            assertThat(user.getDateOfBirth()).isEqualTo(newBirthDate);
            assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/new.png");
        }

        @Test
        @DisplayName("프로필 수정으로는 계정 상태가 바뀌지 않는다")
        void updateProfileDoesNotChangeStatus() {
            User user = UserFixture.activeUser(1L);

            user.updateProfile("New", "Name", null, null, null, null);

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getEmailVerified()).isTrue();
        }

    }

    @Nested
    @DisplayName("로그인 정책")
    class LoginPolicy {

        @Test
        @DisplayName("로그인 기록 시 lastLoginAt 이 갱신된다")
        void recordLogin() {
            User user = UserFixture.activeUser(1L);
            assertThat(user.getLastLoginAt()).isNull();

            user.recordLogin();

            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("상태와 인증 여부를 부분 갱신할 수 있다")
        void updateLoginPolicyPartially() {
            User user = UserFixture.persistedUser(1L, UserStatus.ACTIVE, false);

            user.updateLoginPolicy(UserStatus.SUSPENDED, null, null);

            assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
            assertThat(user.getEmailVerified()).isFalse();
            assertThat(user.getPhoneVerified()).isFalse();
        }

        @Test
        @DisplayName("이메일/휴대폰 인증 여부를 갱신할 수 있다")
        void updateVerificationFlags() {
            User user = UserFixture.persistedUser(1L, UserStatus.ACTIVE, false);

            user.updateLoginPolicy(null, true, true);

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getEmailVerified()).isTrue();
            assertThat(user.getPhoneVerified()).isTrue();
        }

    }

    @Nested
    @DisplayName("주문 가능 여부")
    class Orderable {

        @Test
        @DisplayName("ACTIVE 이고 이메일 인증을 마친 사용자는 주문할 수 있다")
        void orderableWhenActiveAndEmailVerified() {
            User user = UserFixture.persistedUser(1L, UserStatus.ACTIVE, true);

            assertThat(user.isOrderable()).isTrue();
        }

        @Test
        @DisplayName("이메일 미인증 사용자는 주문할 수 없다")
        void notOrderableWhenEmailNotVerified() {
            User user = UserFixture.persistedUser(1L, UserStatus.ACTIVE, false);

            assertThat(user.isActive()).isTrue();
            assertThat(user.isOrderable()).isFalse();
        }

        @Test
        @DisplayName("비활성 사용자는 주문할 수 없다")
        void notOrderableWhenNotActive() {
            User user = UserFixture.persistedUser(1L, UserStatus.SUSPENDED, true);

            assertThat(user.isActive()).isFalse();
            assertThat(user.isOrderable()).isFalse();
        }

        @Test
        @DisplayName("DELETED 상태는 삭제된 사용자로 판별된다")
        void deletedUser() {
            User user = UserFixture.persistedUser(1L, UserStatus.DELETED, true);

            assertThat(user.isDeleted()).isTrue();
        }

    }

}
