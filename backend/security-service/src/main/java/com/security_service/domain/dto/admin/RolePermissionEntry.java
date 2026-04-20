package com.security_service.domain.dto.admin;

import lombok.Builder;

@Builder
public record RolePermissionEntry(

        Long permissionId,

        String attribute,

        String operation,

        Boolean forMe,

        Boolean forAll
) {
}
