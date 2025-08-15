package com.msa.commerce.monolith.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.msa.commerce.monolith.user.entity.QUser;
import com.querydsl.core.types.dsl.EntityPathBase;

@DisplayName("QueryDSL QUser 클래스 생성 검증 테스트")
class QUserGenerationTest {

    @Test
    @DisplayName("QUser 클래스가 정상적으로 생성되고 접근 가능한지 확인")
    void testQUserClassExists() {
        // Given & When: QUser 클래스 인스턴스 생성
        QUser qUser = QUser.user;

        // Then: QUser 클래스가 정상적으로 생성되었는지 확인
        assertThat(qUser).isNotNull();
        assertThat(qUser).isInstanceOf(EntityPathBase.class);
    }

    @Test
    @DisplayName("QUser의 정적 인스턴스가 올바르게 생성되는지 확인")
    void testQUserStaticInstance() {
        // Given & When
        QUser staticInstance = QUser.user;
        QUser newInstance = new QUser("userAlias");

        // Then
        assertThat(staticInstance).isNotNull();
        assertThat(newInstance).isNotNull();
        assertThat(staticInstance.getClass()).isEqualTo(newInstance.getClass());
    }

    @Test
    @DisplayName("QUser의 모든 필드가 올바르게 생성되는지 확인")
    void testQUserFields() {
        // Given
        QUser qUser = QUser.user;

        // When & Then: 모든 필드가 null이 아니고 올바른 타입인지 확인
        assertThat(qUser.id).isNotNull();
        assertThat(qUser.username).isNotNull();
        assertThat(qUser.email).isNotNull();
        assertThat(qUser.password).isNotNull();
        assertThat(qUser.phoneNumber).isNotNull();
        assertThat(qUser.firstName).isNotNull();
        assertThat(qUser.lastName).isNotNull();
        assertThat(qUser.birthDate).isNotNull();
        assertThat(qUser.gender).isNotNull();
        assertThat(qUser.status).isNotNull();
        assertThat(qUser.emailVerified).isNotNull();
        assertThat(qUser.phoneVerified).isNotNull();
        assertThat(qUser.lastLoginAt).isNotNull();
        assertThat(qUser.createdAt).isNotNull();
        assertThat(qUser.updatedAt).isNotNull();
    }

    @Test
    @DisplayName("QUser enum 필드들이 올바른 타입으로 생성되는지 확인")
    void testQUserEnumFields() {
        // Given
        QUser qUser = QUser.user;

        // When & Then: enum 필드들의 타입 확인
        assertThat(qUser.gender.getType()).isEqualTo(Gender.class);
        assertThat(qUser.status.getType()).isEqualTo(UserStatus.class);
    }

    @Test
    @DisplayName("QUser 생성자들이 정상적으로 작동하는지 확인")
    void testQUserConstructors() {
        // Given & When
        QUser defaultQUser = QUser.user;
        QUser variableQUser = new QUser("customUser");

        // Then
        assertThat(defaultQUser).isNotNull();
        assertThat(variableQUser).isNotNull();

        // 필드들이 모두 정상적으로 초기화되었는지 확인
        assertThat(defaultQUser.id).isNotNull();
        assertThat(variableQUser.id).isNotNull();
    }

}