package com.msa.commerce.monolith.user.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.msa.commerce.monolith.config.TestBeansConfiguration;
import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;
import com.msa.commerce.monolith.user.fixture.UserFixture;

@DataJpaTest
@Import({UserRepositoryImpl.class, TestBeansConfiguration.class})
@ActiveProfiles("test")
@DisplayName("UserRepositoryImpl 통합 테스트")
class UserRepositoryImplTest {

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    @DisplayName("사용자를 저장하면 ID 가 발급되고 모든 필드가 보존된다")
    void saveUser() {
        User saved = userRepository.save(UserFixture.newUser());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserUuid()).isNotBlank();
        assertThat(saved.getUsername()).isEqualTo(UserFixture.USERNAME);
        assertThat(saved.getEmail()).isEqualTo(UserFixture.EMAIL);
        assertThat(saved.getPasswordHash()).isEqualTo(UserFixture.PASSWORD_HASH);
        assertThat(saved.getDateOfBirth()).isEqualTo(UserFixture.DATE_OF_BIRTH);
        assertThat(saved.getGender()).isEqualTo(Gender.MALE);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getEmailVerified()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("저장된 사용자를 ID/이메일/username 으로 조회한다")
    void findUser() {
        User saved = userRepository.save(UserFixture.newUser());

        assertThat(userRepository.findById(saved.getId())).isPresent();
        assertThat(userRepository.findByEmail(UserFixture.EMAIL)).isPresent();
        assertThat(userRepository.findByUsername(UserFixture.USERNAME)).isPresent();
        assertThat(userRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("기존 사용자를 수정하면 새 행이 생기지 않고 값이 갱신된다")
    void updateUser() {
        User saved = userRepository.save(UserFixture.newUser());

        saved.updateProfile("Changed", null, null, null, null, null);
        saved.updateLoginPolicy(UserStatus.SUSPENDED, true, null);
        User updated = userRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getFirstName()).isEqualTo("Changed");
        assertThat(updated.getLastName()).isEqualTo("You");
        assertThat(updated.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(updated.getEmailVerified()).isTrue();
        assertThat(userRepository.findAllByIds(List.of(saved.getId()))).hasSize(1);
    }

    @Test
    @DisplayName("로그인 기록이 영속화된다")
    void recordLogin() {
        User saved = userRepository.save(UserFixture.newUser());
        assertThat(saved.getLastLoginAt()).isNull();

        saved.recordLogin();
        User updated = userRepository.save(saved);

        assertThat(updated.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("여러 ID 로 사용자를 한 번에 조회한다")
    void findAllByIds() {
        User first = userRepository.save(UserFixture.newUser());
        User second = userRepository.save(UserFixture.defaultBuilder()
            .username("second")
            .email("second@example.com")
            .build());

        List<User> found = userRepository.findAllByIds(List.of(first.getId(), second.getId(), 999L));

        assertThat(found).hasSize(2);
        assertThat(found).extracting(User::getUsername).containsExactlyInAnyOrder(UserFixture.USERNAME, "second");
    }

    @Test
    @DisplayName("username/email 중복 여부를 확인한다")
    void existsBy() {
        userRepository.save(UserFixture.newUser());

        assertThat(userRepository.existsByUsername(UserFixture.USERNAME)).isTrue();
        assertThat(userRepository.existsByEmail(UserFixture.EMAIL)).isTrue();
        assertThat(userRepository.existsByUsername("unknown")).isFalse();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

}
