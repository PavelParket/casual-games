package com.casualgames.securityservice.domain.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserPermissionUpdateRequest(

        @NotNull Boolean forMe,

        @NotNull Boolean forAll,

        @NotNull Boolean allowed
) {
}
