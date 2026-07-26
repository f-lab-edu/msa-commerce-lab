package com.msa.commerce.monolith.user.application.port.in;

import java.time.LocalDate;

import com.msa.commerce.monolith.user.domain.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCreateCommand {

    @NotBlank(message = "Username is required.")
    @Size(max = 50, message = "Username cannot exceed 50 characters.")
    private final String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private final String email;

    // 평문 비밀번호. 서비스 계층에서 해싱된 뒤 저장되며 도메인/응답으로 노출되지 않는다.
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
    private final String password;

    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name cannot exceed 100 characters.")
    private final String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 100, message = "Last name cannot exceed 100 characters.")
    private final String lastName;

    @Pattern(regexp = "^[0-9-+]{9,20}$", message = "Invalid phone number format.")
    private final String phoneNumber;

    @Past(message = "Date of birth must be in the past.")
    private final LocalDate dateOfBirth;

    private final Gender gender;

    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters.")
    private final String profileImageUrl;

}
