package com.casualgames.securityservice.domain.dto;

import com.casualgames.securitystarter.annotation.Permission;
import com.casualgames.securitystarter.enums.Permissions;
import com.casualgames.securitystarter.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    @Permission(Permissions.GUID)
    private UUID guid;

    @Permission(Permissions.USERNAME)
    private String username;

    @Permission(Permissions.EMAIL)
    private String email;

    @Permission(Permissions.ROLE)
    private Role role;

    private Instant createdAt;
}
