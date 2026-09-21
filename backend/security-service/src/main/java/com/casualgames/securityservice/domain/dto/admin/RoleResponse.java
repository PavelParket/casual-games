package com.casualgames.securityservice.domain.dto.admin;

import lombok.Builder;

@Builder
public record RoleResponse(

        Long id,

        String name
) {
}
