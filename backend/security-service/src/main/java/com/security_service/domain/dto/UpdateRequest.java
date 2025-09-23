package com.security_service.domain.dto;

import lombok.Builder;

@Builder
public record UpdateRequest(
        String username,

        String email,

        String password,

        String role
) {
}
