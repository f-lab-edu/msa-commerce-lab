package com.msa.commerce.monolith.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import com.msa.commerce.common.util.UuidGenerator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private Long id;

    private String userUuid;

    private String username;

    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private Gender gender;

    private UserStatus status;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private String profileImageUrl;

    private LocalDateTime lastLoginAt;

    // createdAt/updatedAt 은 JPA Auditing 이 채운다. 도메인에서는 조회 전용이다.
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public User(String username, String email, String passwordHash, String firstName, String lastName,
        String phoneNumber, LocalDate dateOfBirth, Gender gender, String profileImageUrl) {
        validateUser(username, email, passwordHash, firstName, lastName);

        this.userUuid = UuidGenerator.generate().toString();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.profileImageUrl = profileImageUrl;
    }

    public static User reconstitute(Long id, String userUuid, String username, String email, String passwordHash,
        String firstName, String lastName, String phoneNumber, LocalDate dateOfBirth, Gender gender,
        UserStatus status, Boolean emailVerified, Boolean phoneVerified, String profileImageUrl,
        LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        User user = new User();
        user.id = id;
        user.userUuid = userUuid;
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.firstName = firstName;
        user.lastName = lastName;
        user.phoneNumber = phoneNumber;
        user.dateOfBirth = dateOfBirth;
        user.gender = gender;
        user.status = status;
        user.emailVerified = emailVerified;
        user.phoneVerified = phoneVerified;
        user.profileImageUrl = profileImageUrl;
        user.lastLoginAt = lastLoginAt;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    // My Page 에서 수정 가능한 프로필 정보만 부분 갱신한다.
    public void updateProfile(String firstName, String lastName, String phoneNumber, LocalDate dateOfBirth,
        Gender gender, String profileImageUrl) {
        updateFieldIfNotNull(firstName, value -> this.firstName = value);
        updateFieldIfNotNull(lastName, value -> this.lastName = value);
        updateFieldIfNotNull(phoneNumber, value -> this.phoneNumber = value);
        updateFieldIfNotNull(dateOfBirth, value -> this.dateOfBirth = value);
        updateFieldIfNotNull(gender, value -> this.gender = value);
        updateFieldIfNotNull(profileImageUrl, value -> this.profileImageUrl = value);
    }

    // Auth Service 가 관리하는 로그인 정책(계정 상태, 인증 여부)을 부분 갱신한다.
    public void updateLoginPolicy(UserStatus status, Boolean emailVerified, Boolean phoneVerified) {
        updateFieldIfNotNull(status, value -> this.status = value);
        updateFieldIfNotNull(emailVerified, value -> this.emailVerified = value);
        updateFieldIfNotNull(phoneVerified, value -> this.phoneVerified = value);
    }

    // lastLoginAt 은 감사 정보가 아니라 로그인 정책이 관리하는 업무 필드라 도메인에서 직접 채운다.
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public boolean isDeleted() {
        return UserStatus.DELETED.equals(this.status);
    }

    // Order Service 검증 기준: 활성 상태이고 이메일 인증을 마친 사용자만 주문할 수 있다.
    public boolean isOrderable() {
        return isActive() && Boolean.TRUE.equals(this.emailVerified);
    }

    private void validateUser(String username, String email, String passwordHash, String firstName, String lastName) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters.");
        }

        if (isBlank(email)) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters.");
        }

        if (isBlank(passwordHash)) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (isBlank(firstName)) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (isBlank(lastName)) {
            throw new IllegalArgumentException("Last name is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

}
