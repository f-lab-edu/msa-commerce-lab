package com.msa.commerce.monolith.user.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.msa.commerce.monolith.user.domain.Gender;
import com.msa.commerce.monolith.user.domain.User;
import com.msa.commerce.monolith.user.domain.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false, unique = true, length = 36)
    private String userUuid;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserJpaEntity fromDomainEntity(User user) {
        UserJpaEntity jpaEntity = fromDomainEntityForCreation(user);
        jpaEntity.id = user.getId();
        return jpaEntity;
    }

    // JPA Auditing 활성화 여부와 무관하게 NOT NULL 인 생성/수정 시각이 채워지도록 도메인의 값을 그대로 싣는다.
    public static UserJpaEntity fromDomainEntityForCreation(User user) {
        UserJpaEntity jpaEntity = new UserJpaEntity();
        jpaEntity.createdAt = user.getCreatedAt();
        jpaEntity.updatedAt = user.getUpdatedAt();
        jpaEntity.userUuid = user.getUserUuid();
        jpaEntity.username = user.getUsername();
        jpaEntity.email = user.getEmail();
        jpaEntity.passwordHash = user.getPasswordHash();
        jpaEntity.firstName = user.getFirstName();
        jpaEntity.lastName = user.getLastName();
        jpaEntity.phoneNumber = user.getPhoneNumber();
        jpaEntity.dateOfBirth = user.getDateOfBirth();
        jpaEntity.gender = user.getGender();
        jpaEntity.status = user.getStatus();
        jpaEntity.emailVerified = user.getEmailVerified();
        jpaEntity.phoneVerified = user.getPhoneVerified();
        jpaEntity.profileImageUrl = user.getProfileImageUrl();
        jpaEntity.lastLoginAt = user.getLastLoginAt();
        return jpaEntity;
    }

    // 영속 상태의 엔티티에 변경분을 반영해 더티 체킹으로 UPDATE 되도록 한다.
    public void applyDomainEntity(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.dateOfBirth = user.getDateOfBirth();
        this.gender = user.getGender();
        this.status = user.getStatus();
        this.emailVerified = user.getEmailVerified();
        this.phoneVerified = user.getPhoneVerified();
        this.profileImageUrl = user.getProfileImageUrl();
        this.lastLoginAt = user.getLastLoginAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public User toDomainEntity() {
        return User.reconstitute(
            this.id,
            this.userUuid,
            this.username,
            this.email,
            this.passwordHash,
            this.firstName,
            this.lastName,
            this.phoneNumber,
            this.dateOfBirth,
            this.gender,
            this.status,
            this.emailVerified,
            this.phoneVerified,
            this.profileImageUrl,
            this.lastLoginAt,
            this.createdAt,
            this.updatedAt
        );
    }

}
