package com.msa.commerce.monolith.user.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;

public final class UserFixture {

    public static final String USERNAME = "joel";

    public static final String EMAIL = "joel@example.com";

    public static final String PASSWORD_HASH = "$2a$10$hashed";

    public static final String FIRST_NAME = "Jaeyoung";

    public static final String LAST_NAME = "You";

    public static final String PHONE_NUMBER = "010-1234-5678";

    public static final String PROFILE_IMAGE_URL = "https://example.com/profile.png";

    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, Month.JANUARY, 1);

    private UserFixture() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static User.UserBuilder defaultBuilder() {
        return User.builder()
            .username(USERNAME)
            .email(EMAIL)
            .passwordHash(PASSWORD_HASH)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .phoneNumber(PHONE_NUMBER)
            .dateOfBirth(DATE_OF_BIRTH)
            .gender(Gender.MALE)
            .profileImageUrl(PROFILE_IMAGE_URL);
    }

    public static User newUser() {
        return defaultBuilder().build();
    }

    public static String usernameOf(Long id) {
        return USERNAME + id;
    }

    public static String emailOf(Long id) {
        return USERNAME + id + "@example.com";
    }

    // 영속화된 사용자를 재현한다. 도메인은 id/status 를 빌더로 받지 않으므로 reconstitute 를 사용한다.
    public static User persistedUser(Long id, UserStatus status, boolean emailVerified) {
        LocalDateTime now = LocalDateTime.now();

        return User.reconstitute(
            id,
            "018f8c2e-0000-7000-8000-00000000000" + id,
            usernameOf(id),
            emailOf(id),
            PASSWORD_HASH,
            FIRST_NAME,
            LAST_NAME,
            PHONE_NUMBER,
            DATE_OF_BIRTH,
            Gender.MALE,
            status,
            emailVerified,
            false,
            PROFILE_IMAGE_URL,
            null,
            now,
            now
        );
    }

    public static User activeUser(Long id) {
        return persistedUser(id, UserStatus.ACTIVE, true);
    }

}
