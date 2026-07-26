package com.msa.commerce.monolith.user.adapter.in.web;

import java.time.LocalDate;

import com.msa.commerce.monolith.user.domain.Gender;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// My Page 프로필 수정용. 계정 상태/인증 여부는 UserLoginPolicyRequest 로 분리되어 있다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100, message = "First name cannot exceed 100 characters.")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters.")
    private String lastName;

    @Pattern(regexp = "^[0-9-+]{9,20}$", message = "Invalid phone number format.")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past.")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters.")
    private String profileImageUrl;

}
