package com.msa.commerce.monolith.user.application.port.in;

import java.time.LocalDate;

import com.msa.commerce.monolith.user.domain.Gender;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateCommand {

    @NotNull(message = "User ID is required.")
    @Positive(message = "User ID must be positive.")
    private final Long userId;

    @Size(max = 100, message = "First name cannot exceed 100 characters.")
    private final String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters.")
    private final String lastName;

    @Pattern(regexp = "^[0-9-+]{9,20}$", message = "Invalid phone number format.")
    private final String phoneNumber;

    @Past(message = "Date of birth must be in the past.")
    private final LocalDate dateOfBirth;

    private final Gender gender;

    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters.")
    private final String profileImageUrl;

    public boolean hasNoChanges() {
        return firstName == null && lastName == null && phoneNumber == null
            && dateOfBirth == null && gender == null && profileImageUrl == null;
    }

}
