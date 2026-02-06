package com.security_starter.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true")
@Slf4j
public class RedisPermissionRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ROLE_PREFIX = "role:";
    private static final String USER_ALLOW_PREFIX = "user:allow:";
    private static final String USER_RESTRICT_PREFIX = "user:restrict:";
    private static final String DELIMITER = ",";

    /**
     * Get base permissions for a role
     * Key format: role:{ROLE_NAME}
     * Value format: comma-separated permissions
     */
    public Set<String> getRolePermissions(String role) {
        try {
            String key = ROLE_PREFIX + role;
            String value = redisTemplate.opsForValue().get(key);
            return parsePermissions(value);
        } catch (Exception e) {
            log.error("Failed to get role permissions for role={}: {}", role, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Get allowed permissions for a user
     * Key format: user:allow:{email}
     */
    public Set<String> getUserAllowedPermissions(String email) {
        try {
            String key = USER_ALLOW_PREFIX + email;
            String value = redisTemplate.opsForValue().get(key);
            return parsePermissions(value);
        } catch (Exception e) {
            log.error("Failed to get allowed permissions for email={}: {}", email, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Get restricted permissions for a user
     * Key format: user:restrict:{email}
     */
    public Set<String> getUserRestrictedPermissions(String email) {
        try {
            String key = USER_RESTRICT_PREFIX + email;
            String value = redisTemplate.opsForValue().get(key);
            return parsePermissions(value);
        } catch (Exception e) {
            log.error("Failed to get restricted permissions for email={}: {}", email, e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Set role permissions
     */
    public void setRolePermissions(String role, Set<String> permissions) {
        try {
            String key = ROLE_PREFIX + role;
            String value = String.join(DELIMITER, permissions);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to set role permissions for role={}: {}", role, e.getMessage());
        }
    }

    /**
     * Set user allowed permissions
     */
    public void setUserAllowedPermissions(String email, Set<String> permissions) {
        try {
            String key = USER_ALLOW_PREFIX + email;
            String value = String.join(DELIMITER, permissions);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to set allowed permissions for email={}: {}", email, e.getMessage());
        }
    }

    /**
     * Set user restricted permissions
     */
    public void setUserRestrictedPermissions(String email, Set<String> permissions) {
        try {
            String key = USER_RESTRICT_PREFIX + email;
            String value = String.join(DELIMITER, permissions);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to set restricted permissions for email={}: {}", email, e.getMessage());
        }
    }

    /**
     * Delete user permissions
     */
    public void deleteUserPermissions(String email) {
        try {
            redisTemplate.delete(USER_ALLOW_PREFIX + email);
            redisTemplate.delete(USER_RESTRICT_PREFIX + email);
        } catch (Exception e) {
            log.error("Failed to delete permissions for email={}: {}", email, e.getMessage());
        }
    }

    private Set<String> parsePermissions(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(value.split(DELIMITER))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toSet());
    }
}
