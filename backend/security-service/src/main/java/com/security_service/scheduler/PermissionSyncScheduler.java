package com.security_service.scheduler;

import com.security_service.repository.PermissionRepository;
import com.security_service.repository.RolePermissionRepository;
import com.security_service.repository.RoleRepository;
import com.security_starter.repository.RedisPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionSyncScheduler {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final RolePermissionRepository rolePermissionRepository;

    private final RedisPermissionRepository redisRepository;

    /**
     * Sync all role permissions from database to Redis
     * Runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 10000)
    @Transactional(readOnly = true)
    public void syncRolePermissions() {
        log.info("Starting role permissions synchronization to Redis");

        try {
            List<Object[]> results = rolePermissionRepository.findAllRolePermissions();
            Map<String, Set<String>> rolePermissionsMap = new HashMap<>();

            for (Object[] row : results) {
                String roleName = (String) row[0];
                String attribute = (String) row[1];
                String operation = (String) row[2];

                String permission = attribute + ":" + operation;

                rolePermissionsMap
                        .computeIfAbsent(roleName, k -> new HashSet<>())
                        .add(permission);
            }

            for (Map.Entry<String, Set<String>> entry : rolePermissionsMap.entrySet()) {
                String role = entry.getKey();
                Set<String> permissions = entry.getValue();

                redisRepository.setRolePermissions(role, permissions);
            }

            log.info("Successfully synced {} roles to Redis", rolePermissionsMap.size());
        } catch (Exception e) {
            log.error("Failed to sync role permissions to Redis", e);
        }
    }

    /**
     * Sync user-specific permissions (allow/restrict)
     * This should be called when user permissions are modified
     */
    @Transactional(readOnly = true)
    public void syncRolePermissions(String roleName) {
        log.info("Syncing permissions for role: {}", roleName);

        try {
            List<Object[]> rolePermissions = rolePermissionRepository.findPermissionsByRoleName(roleName);
            Set<String> permissions = rolePermissions.stream()
                    .map(row -> row[0] + ":" + row[1])
                    .collect(Collectors.toSet());

            redisRepository.setRolePermissions(roleName, permissions);
            log.info("Synced {} permissions for role {}", permissions.size(), roleName);

        } catch (Exception e) {
            log.error("Failed to sync permissions for role: {}", roleName, e);
        }
    }

    /**
     * Sync user-specific permissions (allow/restrict)
     * This should be called when user permissions are modified
     */
    public void syncUserPermissions(String email, Set<String> allowedPermissions, Set<String> restrictedPermissions) {
        log.info("Syncing custom permissions for user: {}", email);

        try {
            if (allowedPermissions != null && !allowedPermissions.isEmpty()) {
                redisRepository.setUserAllowedPermissions(email, allowedPermissions);
            }

            if (restrictedPermissions != null && !restrictedPermissions.isEmpty()) {
                redisRepository.setUserRestrictedPermissions(email, restrictedPermissions);
            }

        } catch (Exception e) {
            log.error("Failed to sync user permissions for: {}", email, e);
        }
    }

    /**
     * Clear user-specific permissions
     */
    public void clearUserPermissions(String email) {
        log.info("Clearing custom permissions for user: {}", email);
        redisRepository.deleteUserPermissions(email);
    }

    /**
     * Manual sync trigger (for admin API)
     */
    public void manualSync() {
        log.info("Manual sync triggered");
        syncRolePermissions();
    }
}
