package com.security_starter.factory;

import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionContextFactory {

    public static PermissionContext create(Role role, Status status, Boolean isOwner, UUID actorGuid, UUID targetGuid) {
        return PermissionContext.builder()
                .role(role)
                .status(status)
                .isOwner(isOwner)
                .actorGuid(actorGuid)
                .targetGuid(targetGuid)
                .build();
    }
}
