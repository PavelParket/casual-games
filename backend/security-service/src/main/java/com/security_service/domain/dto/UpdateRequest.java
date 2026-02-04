package com.security_service.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdateRequest(
        @Length(max = 50)
        String username,

        @Email
        @Length(max = 200)
        String email,

        @Length(min = 4)
        String password,

        String role
) {
}
