package com.security_service.domain.dto.admin;

import lombok.Builder;

@Builder
public record RoleResponse(

        Long id,

        String name
) {
}
