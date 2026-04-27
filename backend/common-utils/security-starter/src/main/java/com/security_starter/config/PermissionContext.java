package com.security_starter.config;

import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionContext {

    private Role role;

    private Status status;

    private boolean isOwner;

    private UUID actorGuid;

    private UUID targetGuid;
}
