package com.security_service.domain.dto.admin;

import lombok.Builder;

import java.util.List;

@Builder
public record RolePermissionResponse(

        Long roleId,

        String roleName,

        List<RolePermissionEntry> permissions
) {
}
