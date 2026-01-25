package com.security_starter.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import com.security_starter.factory.PermissionContextFactory;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@UtilityClass
public class PermissionContextHelper {

    /**
     * Get current authentication from SecurityContext
     */
    public static AuthenticationToken getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AuthenticationToken) {
            return (AuthenticationToken) authentication;
        }

        return null;
    }

    /**
     * Create PermissionContext from current authentication
     */
    public static PermissionContext createContextFromAuthentication(UUID targetGuid) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null) {
            return null;
        }

        // Extract primary role (first role from set)
        Role role = auth.getRoles().stream()
                .findFirst()
                .map(Role::valueOf)
                .orElse(null);

        Status status = auth.getStatus();
        UUID actorGuid = auth.getGuid();
        boolean isOwner = actorGuid != null && actorGuid.equals(targetGuid);

        return PermissionContextFactory.create(role, status, isOwner, actorGuid, targetGuid);
    }

    /**
     * Create PermissionContext with explicit parameters
     */
    public static PermissionContext createContext(
            Role role,
            Status status,
            boolean isOwner,
            UUID actorGuid,
            UUID targetGuid
    ) {
        return PermissionContextFactory.create(role, status, isOwner, actorGuid, targetGuid);
    }

    /**
     * Check if current user is owner of the resource
     */
    public static boolean isOwner(UUID resourceOwnerGuid) {
        AuthenticationToken auth = getCurrentAuthentication();

        if (auth == null || resourceOwnerGuid == null) {
            return false;
        }

        return auth.getGuid().equals(resourceOwnerGuid);
    }

    /**
     * Get current user GUID
     */
    public static UUID getCurrentUserGuid() {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null ? auth.getGuid() : null;
    }

    /**
     * Get current user email
     */
    public static String getCurrentUserEmail() {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null ? auth.getEmail() : null;
    }

    /**
     * Check if current user has permission
     */
    public static boolean hasPermission(String permission) {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null && auth.hasPermission(permission);
    }

    /**
     * Check if current user has role
     */
    public static boolean hasRole(Role role) {
        AuthenticationToken auth = getCurrentAuthentication();
        return auth != null && auth.hasRole(role);
    }
}
