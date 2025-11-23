package com.security_service.domain.dto.user_service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Builder
public record CreateUserInternalRequest(
        @NotBlank(message = "GUID cannot be empty")
        UUID guid,

        @NotBlank(message = "Username cannot be empty")
        @Length(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$")
        String username,

        @NotBlank(message = "Email cannot be empty")
        @Email
        @Length(max = 200)
        String email
) {
}
