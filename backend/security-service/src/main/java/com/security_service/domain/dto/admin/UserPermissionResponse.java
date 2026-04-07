package com.security_service.domain.dto.admin;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPermissionResponse(

        Long id,

        UUID userGuid,

        String email,

        Long permissionId,

        String attribute,

        String operation,

        Boolean forMe,

        Boolean forAll,

        Boolean allowed
) {
}
