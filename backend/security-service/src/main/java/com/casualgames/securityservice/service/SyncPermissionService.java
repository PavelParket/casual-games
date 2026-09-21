package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securityservice.domain.entity.User;
import com.casualgames.securityservice.repository.RolePermissionRepository;
import com.casualgames.securityservice.repository.UserPermissionRepository;
import com.casualgames.securityservice.repository.UserRepository;
import com.casualgames.securityservice.repository.projection.FullUserPermissionProjection;
import com.casualgames.securityservice.repository.projection.RolePermissionProjection;
import com.casualgames.securityservice.repository.projection.UserPermissionProjection;
import com.casualgames.securityservice.repository.redis.UserPermissionRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncPermissionService {

    public static final String UNDERSCORE = "_";
    public static final String FOR_ME_POSTFIX = "FOR_ME";
    public static final String FOR_ALL_POSTFIX = "FOR_ALL";
    public static final String WITHOUT_ME_POSTFIX = "WITHOUT_ME";

    private final RolePermissionRepository rolePermissionRepository;

    private final UserPermissionRepository userPermissionRepository;

    private final UserRepository userRepository;

    private final UserPermissionRedisRepository userPermissionRedisRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void syncAllPermissionsToRedis() {
        log.info("Starting full Redis permission sync...");
        try {
            syncAllRoles();
            syncAllUserPermissions();
            log.info("Full Redis permission sync completed successfully");
        } catch (Exception e) {
            log.error("Failed to complete full Redis permission sync", e);
        }
    }

    @Transactional(readOnly = true)
    public void syncUserPermissions(UUID userGuid) {
        User user = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException("User not found for guid=" + userGuid));

        List<UserPermissionProjection> userPermissions = userPermissionRepository.findAllWithPermissionByUserGuid(userGuid);

        writeUserPermissionsToRedis(user.getEmail(), userPermissions);

        log.info("Synced permission overrides for user guid={}", userGuid);
    }

    private void syncAllRoles() {
        List<RolePermissionProjection> rolePermissions = rolePermissionRepository.findAllRolePermissions();

        Map<String, Set<String>> rolePermissionsMap = new HashMap<>();

        rolePermissions.forEach(rolePermission -> {
            String key = buildPermissionKey(
                    rolePermission.getAttribute(),
                    rolePermission.getOperation(),
                    rolePermission.getForMe(),
                    rolePermission.getForAll()
            );

            if (key != null) {
                rolePermissionsMap.computeIfAbsent(
                                rolePermission.getRoleName(),
                                permissions -> new HashSet<>()
                        )
                        .add(key);
            }
        });

        rolePermissionsMap.forEach(userPermissionRedisRepository::setRolePermissions);

        log.info("Synced {} roles to Redis", rolePermissionsMap.size());
    }

    private void syncAllUserPermissions() {
        List<FullUserPermissionProjection> userPermissions = userPermissionRepository.findAllForFullSync();

        Map<String, List<FullUserPermissionProjection>> userPermissionsMap = new HashMap<>();

        userPermissions.forEach(userPermission ->
                userPermissionsMap.computeIfAbsent(userPermission.getEmail(), permissions -> new ArrayList<>())
                .add(userPermission));

        userPermissionsMap.forEach((email, userPermissionList) -> {
            Set<String> allowed = new HashSet<>();
            Set<String> restricted = new HashSet<>();

            userPermissionList.forEach(userPermission -> {
                String key = buildPermissionKey(
                        userPermission.getAttribute(),
                        userPermission.getOperation(),
                        userPermission.getForMe(),
                        userPermission.getForAll()
                );

                if (key != null) {
                    if (Boolean.TRUE.equals(userPermission.getAllowed())) {
                        allowed.add(key);
                    } else {
                        restricted.add(key);
                    }
                }
            });

            userPermissionRedisRepository.deleteUserPermissions(email);

            if (!allowed.isEmpty()) {
                userPermissionRedisRepository.setUserAllowedPermissions(email, allowed);
            }

            if (!restricted.isEmpty()) {
                userPermissionRedisRepository.setUserRestrictedPermissions(email, restricted);
            }
        });

        log.info("Synced user permissions for {} users to Redis", userPermissionsMap.size());
    }

    private void writeUserPermissionsToRedis(String email, List<UserPermissionProjection> userPermissionProjections) {
        Set<String> allowed = new HashSet<>();
        Set<String> restricted = new HashSet<>();

        userPermissionProjections.forEach(userPermission -> {
            String key = buildPermissionKey(
                    userPermission.getAttribute(),
                    userPermission.getOperation(),
                    userPermission.getForMe(),
                    userPermission.getForAll()
            );

            if (key != null) {
                if (Boolean.TRUE.equals(userPermission.getAllowed())) {
                    allowed.add(key);
                } else {
                    restricted.add(key);
                }
            }
        });

        userPermissionRedisRepository.deleteUserPermissions(email);

        if (!allowed.isEmpty()) {
            userPermissionRedisRepository.setUserAllowedPermissions(email, allowed);
        }

        if (!restricted.isEmpty()) {
            userPermissionRedisRepository.setUserRestrictedPermissions(email, restricted);
        }
    }

    private String buildPermissionKey(String attribute, String operation, Boolean forMe, Boolean forAll) {
        if (forMe && forAll) {
            return String.join(UNDERSCORE, attribute, operation, FOR_ALL_POSTFIX);
        } else if (forMe) {
            return String.join(UNDERSCORE, attribute, operation, FOR_ME_POSTFIX);
        } else if (forAll) {
            return String.join(UNDERSCORE, attribute, operation, WITHOUT_ME_POSTFIX);
        } else {
            return null;
        }
    }
}
