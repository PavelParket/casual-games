package com.security_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email
        @Length(max = 200)
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Length(min = 4)
        String password
) {
}
