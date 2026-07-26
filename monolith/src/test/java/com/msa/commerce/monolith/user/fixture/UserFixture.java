package com.msa.commerce.monolith.user.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;

public final class UserFixture {

    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);

    private UserFixture() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static User.UserBuilder defaultBuilder() {
        return User.builder()
            .username("joel")
            .email("joel@example.com")
            .passwordHash("$2a$10$hashed")
            .firstName("Jaeyoung")
            .lastName("You")
            .phoneNumber("010-1234-5678")
            .dateOfBirth(DATE_OF_BIRTH)
            .gender(Gender.MALE)
            .profileImageUrl("https://example.com/profile.png");
    }

    public static User newUser() {
        return defaultBuilder().build();
    }

    // 영속화된 사용자를 재현한다. 도메인은 id/status 를 빌더로 받지 않으므로 reconstitute 를 사용한다.
    public static User persistedUser(Long id, UserStatus status, boolean emailVerified) {
        LocalDateTime now = LocalDateTime.now();

        return User.reconstitute(
            id,
            "018f8c2e-0000-7000-8000-00000000000" + id,
            "joel" + id,
            "joel" + id + "@example.com",
            "$2a$10$hashed",
            "Jaeyoung",
            "You",
            "010-1234-5678",
            DATE_OF_BIRTH,
            Gender.MALE,
            status,
            emailVerified,
            false,
            "https://example.com/profile.png",
            null,
            now,
            now
        );
    }

    public static User activeUser(Long id) {
        return persistedUser(id, UserStatus.ACTIVE, true);
    }

}
