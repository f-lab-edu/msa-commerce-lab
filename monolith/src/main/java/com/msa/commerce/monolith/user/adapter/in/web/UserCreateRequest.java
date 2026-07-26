package com.msa.commerce.monolith.user.adapter.in.web;

import java.time.LocalDate;

import com.msa.commerce.monolith.user.domain.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Username is required.")
    @Size(max = 50, message = "Username cannot exceed 50 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
    private String password;

    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name cannot exceed 100 characters.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
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
