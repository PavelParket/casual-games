package com.security_service.domain.dto;

import com.security_starter.annotation.Permission;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
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
