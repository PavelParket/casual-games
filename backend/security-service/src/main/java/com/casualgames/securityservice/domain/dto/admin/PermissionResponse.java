package com.casualgames.securityservice.domain.dto.admin;

import lombok.Builder;

@Builder
public record PermissionResponse(

        Long id,

        String attribute,

        String operation
) {
}
