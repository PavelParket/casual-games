package com.security_starter.provider;

import com.security_starter.repository.RedisPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class DefaultPermissionProvider implements PermissionProvider {

    private final RedisPermissionRepository repository;

    @Override
    public Map<String, Set<String>> getPermissions(Set<String> roles, String email) {
        Map<String, Set<String>> rolePermissionsMap = new HashMap<>();

        Set<String> allowedPermissions = repository.getUserAllowedPermissions(email);
        Set<String> restrictedPermissions = repository.getUserRestrictedPermissions(email);

        for (String role : roles) {
            Set<String> permissions = new HashSet<>(repository.getRolePermissions(role));

            permissions.addAll(allowedPermissions);
            permissions.removeAll(restrictedPermissions);

            rolePermissionsMap.put(role, permissions);
        }

        return rolePermissionsMap;
    }
}
