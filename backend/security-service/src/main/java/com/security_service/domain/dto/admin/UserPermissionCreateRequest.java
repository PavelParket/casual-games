package com.security_service.domain.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPermissionCreateRequest(

        @NotNull UUID userGuid,

        @NotNull Long permissionId,

        @NotNull Boolean forMe,

        @NotNull Boolean forAll,

        @NotNull Boolean allowed
) {
}
