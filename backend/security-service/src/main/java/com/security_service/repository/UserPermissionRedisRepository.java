package com.security_service.repository;

import com.redis_starter.repository.RedisHashRepository;
import com.security_starter.enums.PermissionRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserPermissionRedisRepository {

    private static final String DELIMITER = ",";

    private final RedisHashRepository redisHashRepository;

    public Set<String> getRolePermissions(String role) {
        try {
            return parse(redisHashRepository.findByKey(PermissionRedisKey.ROLE.getKey(), role));
        } catch (Exception e) {
            log.error("Failed to get role permissions for role={}: {}", role, e.getMessage());
            return Collections.emptySet();
        }
    }

    public void setRolePermissions(String role, Set<String> permissions) {
        try {
            redisHashRepository.put(PermissionRedisKey.ROLE.getKey(), role, String.join(DELIMITER, permissions));
        } catch (Exception e) {
            log.error("Failed to set role permissions for role={}: {}", role, e.getMessage());
        }
    }

    public Set<String> getUserAllowedPermissions(String email) {
        try {
            return parse(redisHashRepository.findByKey(PermissionRedisKey.USER_ALLOW.getKey(), email));
        } catch (Exception e) {
            log.error("Failed to get allowed permissions for email={}: {}", email, e.getMessage());
            return Collections.emptySet();
        }
    }

    public void setUserAllowedPermissions(String email, Set<String> permissions) {
        try {
            redisHashRepository.put(PermissionRedisKey.USER_ALLOW.getKey(), email, String.join(DELIMITER, permissions));
        } catch (Exception e) {
            log.error("Failed to set allowed permissions for email={}: {}", email, e.getMessage());
        }
    }

    public Set<String> getUserRestrictedPermissions(String email) {
        try {
            return parse(redisHashRepository.findByKey(PermissionRedisKey.USER_RESTRICT.getKey(), email));
        } catch (Exception e) {
            log.error("Failed to get restricted permissions for email={}: {}", email, e.getMessage());
            return Collections.emptySet();
        }
    }

    public void setUserRestrictedPermissions(String email, Set<String> permissions) {
        try {
            redisHashRepository.put(PermissionRedisKey.USER_RESTRICT.getKey(), email, String.join(DELIMITER, permissions));
        } catch (Exception e) {
            log.error("Failed to set restricted permissions for email={}: {}", email, e.getMessage());
        }
    }

    public void deleteUserPermissions(String email) {
        try {
            redisHashRepository.delete(PermissionRedisKey.USER_ALLOW.getKey(), email);
            redisHashRepository.delete(PermissionRedisKey.USER_RESTRICT.getKey(), email);
        } catch (Exception e) {
            log.error("Failed to delete permissions for email={}: {}", email, e.getMessage());
        }
    }

    private Set<String> parse(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(value.split(DELIMITER))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toSet());
    }
}
