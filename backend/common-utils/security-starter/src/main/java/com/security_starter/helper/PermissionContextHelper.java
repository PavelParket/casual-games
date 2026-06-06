package com.security_starter.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import com.security_starter.factory.PermissionContextFactory;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.security_starter.enums.OperationPostfix.FOR_ALL;
import static com.security_starter.validator.PermissionValidator.UNDERSCORE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionContextHelper {

    private final PermissionValidator permissionValidator;

    public AuthenticationToken getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AuthenticationToken) {
            return (AuthenticationToken) authentication;
        }

        return null;
    }

    public PermissionContext createContextFromAuthentication(UUID targetGuid) {
        // todo: убрать и прокидывать токен в параметрах метода
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null) {
            return null;
        }

        // Extract primary role (first role from set)
        Role role = auth.getRoles().stream()
                .findFirst()
                .map(r -> {
                    try {
                        return Role.valueOf(r);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown role in token: {}", r);
                        return null;
                    }
                })
                .orElse(null);

        Status status = auth.getStatus();
        UUID actorGuid = auth.getGuid();
        boolean isOwner = actorGuid != null && actorGuid.equals(targetGuid);

        return PermissionContextFactory.create(role, status, isOwner, actorGuid, targetGuid);
    }

    public boolean isOwner(UUID resourceOwnerGuid) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null || resourceOwnerGuid == null) {
            return false;
        }

        return auth.getGuid().equals(resourceOwnerGuid);
    }

    public UUID getCurrentUserGuid() {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null ? auth.getGuid() : null;
    }

    public UUID getCurrentUserTokenSid() {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null ? auth.getSid() : null;
    }

    public String getCurrentUserEmail() {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null ? auth.getEmail() : null;
    }

    public boolean hasPermission(Permissions permission, Operation operation) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null) {
            return false;
        }

        return permissionValidator.getPermissions(permission, operation).stream()
                .anyMatch(auth::hasPermission);
    }

    public boolean hasPermission(Permissions permission, Operation operation, UUID targetUserGuid) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null) {
            return false;
        }

        PermissionContext context = createContextFromAuthentication(targetUserGuid);

        return permissionValidator.can(permission, operation, context, auth);
    }

    public boolean hasPermissionForAll(Permissions permissions, Operation operation) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null) {
            return false;
        }

        return auth.hasPermission(String.join(UNDERSCORE, permissions.name(), operation.name(), FOR_ALL.name()));
    }
}
