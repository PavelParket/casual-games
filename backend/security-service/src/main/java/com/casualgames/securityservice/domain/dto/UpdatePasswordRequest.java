package com.casualgames.securityservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UpdatePasswordRequest(

        @NotBlank
        @Length(min = 4)
        String newPassword
) {
}
