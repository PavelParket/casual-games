package com.security_service.domain.dto.admin;

import lombok.Builder;

@Builder
public record PermissionResponse(

        Long id,

        String attribute,

        String operation
) {
}
