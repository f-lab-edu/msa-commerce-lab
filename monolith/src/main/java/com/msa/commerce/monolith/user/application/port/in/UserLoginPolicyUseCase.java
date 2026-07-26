package com.msa.commerce.monolith.user.application.port.in;

// Auth Service 전용. My Page 프로필 수정과 권한/책임을 분리하기 위해 별도 UseCase 로 둔다.
public interface UserLoginPolicyUseCase {

    UserResponse recordLogin(Long userId);

    UserResponse updateLoginPolicy(UserLoginPolicyCommand command);

}
