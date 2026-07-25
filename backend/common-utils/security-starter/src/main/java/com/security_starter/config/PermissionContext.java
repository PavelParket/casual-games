package com.security_starter.config;

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

    private boolean isOwner;

    private boolean isAdmin;

    private UUID actorGuid;

    private UUID targetGuid;
}
