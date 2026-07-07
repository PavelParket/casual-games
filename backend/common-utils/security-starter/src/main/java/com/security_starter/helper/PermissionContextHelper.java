package com.security_starter.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.OperationPostfix;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import com.security_starter.provider.PermissionProvider;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionContextHelper {

    private final PermissionProvider permissionProvider;

    private final PermissionValidator permissionValidator;

    public Optional<AuthenticationToken> getCurrentAuthentication() {
        return permissionProvider.getToken();
    }

    public PermissionContext createContextFromAuthentication(AuthenticationToken authenticationToken, UUID targetGuid) {
        if (authenticationToken == null) {
            return null;
        }

        // Extract primary role (first role from set)
        Role role = authenticationToken.getRoles().stream()
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

        Status status = authenticationToken.getStatus();
        UUID actorGuid = authenticationToken.getGuid();
        boolean isOwner = Objects.equals(actorGuid, targetGuid);

        return PermissionContext.builder()
                .role(role)
                .status(status)
                .isOwner(isOwner)
                .actorGuid(actorGuid)
                .targetGuid(targetGuid)
                .build();
    }

    public PermissionContext createContextFromAuthentication(UUID targetGuid) {
        return getCurrentAuthentication()
                .map(token -> createContextFromAuthentication(token, targetGuid))
                .orElse(null);
    }

    public boolean isOwner(AuthenticationToken token, UUID resourceOwnerGuid) {
        if (token == null || resourceOwnerGuid == null) {
            return false;
        }

        return Objects.equals(token.getGuid(), resourceOwnerGuid);
    }

    public boolean isOwner(UUID resourceOwnerGuid) {
        return getCurrentAuthentication()
                .map(token -> isOwner(token, resourceOwnerGuid))
                .orElse(false);
    }

    public UUID getCurrentUserGuid(AuthenticationToken token) {
        return token != null ? token.getGuid() : null;
    }

    public UUID getCurrentUserGuid() {
        return getCurrentAuthentication()
                .map(this::getCurrentUserGuid)
                .orElse(null);
    }

    public UUID getCurrentUserTokenSid(AuthenticationToken token) {
        return token != null ? token.getSid() : null;
    }

    public UUID getCurrentUserTokenSid() {
        return getCurrentAuthentication()
                .map(this::getCurrentUserTokenSid)
                .orElse(null);
    }

    public String getCurrentUserEmail(AuthenticationToken token) {
        return token != null ? token.getEmail() : null;
    }

    public String getCurrentUserEmail() {
        return getCurrentAuthentication()
                .map(this::getCurrentUserEmail)
                .orElse(null);
    }

    public boolean hasPermission(AuthenticationToken token, Permissions permission, Operation operation) {
        if (token == null) {
            return false;
        }

        return permissionValidator.getPermissions(permission, operation).stream()
                .anyMatch(token::hasPermission);
    }

    public boolean hasPermission(Permissions permission, Operation operation) {
        return getCurrentAuthentication()
                .map(token -> hasPermission(token, permission, operation))
                .orElse(false);
    }

    public boolean hasPermission(AuthenticationToken token, Permissions permission, Operation operation, UUID targetUserGuid) {
        if (token == null) {
            return false;
        }

        PermissionContext context = createContextFromAuthentication(token, targetUserGuid);

        return permissionValidator.hasAccess(permission, operation, context, token);
    }

    public boolean hasPermission(Permissions permission, Operation operation, UUID targetUserGuid) {
        return getCurrentAuthentication()
                .map(token -> hasPermission(token, permission, operation, targetUserGuid))
                .orElse(false);
    }

    public boolean hasPermissionForAll(Permissions permission, Operation operation) {
        return getCurrentAuthentication()
                .map(authenticationToken -> {
                            String key = permissionValidator.getPermission(permission, operation, OperationPostfix.FOR_ALL);
                            return key != null && authenticationToken.hasPermission(key);
                        }
                )
                .orElse(false);
    }
}
