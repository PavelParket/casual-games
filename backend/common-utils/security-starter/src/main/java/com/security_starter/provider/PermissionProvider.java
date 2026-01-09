package com.security_starter.provider;

import java.util.Map;
import java.util.Set;

public interface PermissionProvider {

    /**
     * Get all permissions for given roles and email
     * Returns map: role -> set of permissions
     */
    Map<String, Set<String>> getPermissions(Set<String> roles, String email);

    /**
     * Get flattened set of all permissions across all roles
     */
    default Set<String> getAllPermissions(Set<String> roles, String email) {
        return getPermissions(roles, email).values()
                .stream()
                .flatMap(Set::stream)
                .collect(java.util.stream.Collectors.toSet());
    }
}
