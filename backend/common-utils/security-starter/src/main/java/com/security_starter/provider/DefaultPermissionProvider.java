package com.security_starter.provider;

import com.redis_starter.repository.RedisHashRepository;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.PermissionRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class DefaultPermissionProvider implements PermissionProvider {

    private static final String DELIMITER = ",";

    private final RedisHashRepository redisHashRepository;

    @Override
    public Set<String> loadPermissions(Set<String> roles, String email) {
        try {
            Set<String> permissions = new HashSet<>();

            roles.forEach(role -> {
                permissions.addAll(readFromHash(PermissionRedisKey.ROLE.getKey(), role));
            });

            permissions.addAll(readFromHash(PermissionRedisKey.USER_ALLOW.getKey(), email));
            permissions.removeAll(readFromHash(PermissionRedisKey.USER_RESTRICT.getKey(), email));

            return Collections.unmodifiableSet(permissions);
        } catch (Exception e) {
            log.error("Failed to load permissions for email={}, roles={}: {}", email, roles, e.getMessage());
            return Set.of();
        }
    }

    @Override
    public AuthenticationToken getToken() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(AuthenticationToken.class::isInstance)
                .map(AuthenticationToken.class::cast)
                .orElse(null);
    }

    private Set<String> readFromHash(String key, String field) {
        String value = redisHashRepository.findByKey(key, field);

        if (value == null || value.isEmpty()) {
            return Set.of();
        }

        return Arrays.stream(value.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
