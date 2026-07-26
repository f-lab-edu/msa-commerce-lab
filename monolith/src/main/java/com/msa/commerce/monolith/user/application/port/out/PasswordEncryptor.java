package com.msa.commerce.monolith.user.application.port.out;

// 애플리케이션 계층이 특정 해싱 구현(BCrypt 등)에 의존하지 않도록 분리한다.
public interface PasswordEncryptor {

    String encrypt(String rawPassword);

    boolean matches(String rawPassword, String encryptedPassword);

}
