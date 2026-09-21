package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.ConflictException;
import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securityservice.domain.dto.admin.UserPermissionCreateRequest;
import com.casualgames.securityservice.domain.dto.admin.UserPermissionResponse;
import com.casualgames.securityservice.domain.dto.admin.UserPermissionUpdateRequest;
import com.casualgames.securityservice.domain.entity.Permission;
import com.casualgames.securityservice.domain.entity.User;
import com.casualgames.securityservice.domain.entity.UserPermission;
import com.casualgames.securityservice.mapper.UserPermissionMapper;
import com.casualgames.securityservice.repository.PermissionRepository;
import com.casualgames.securityservice.repository.UserPermissionRepository;
import com.casualgames.securityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.casualgames.securityservice.config.ResourceMessageConstants.CONFLICT_USER_PERMISSION;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_PERMISSION;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER;
import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_USER_PERMISSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;

    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;

    private final SyncPermissionService syncPermissionService;

    private final UserPermissionMapper userPermissionMapper;

    @Transactional(readOnly = true)
    public List<UserPermissionResponse> getByUserGuid(UUID userGuid) {
        User user = userRepository.findByGuid(userGuid)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        return userPermissionRepository.findAllWithPermissionByUserGuid(userGuid).stream()
                .map(userPermission -> userPermissionMapper.toResponse(user, userPermission))
                .toList();
    }

    @Transactional
    public UserPermissionResponse create(UserPermissionCreateRequest userPermissionRequest) {
        User user = userRepository.findByGuid(userPermissionRequest.userGuid())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        Permission permission = permissionRepository.findById(userPermissionRequest.permissionId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_PERMISSION));

        userPermissionRepository.findByUserGuidAndPermissionId(user.getGuid(), userPermissionRequest.permissionId())
                .ifPresent(userPermission -> {
                    throw new ConflictException(CONFLICT_USER_PERMISSION);
                });

        UserPermission saved = userPermissionRepository.save(userPermissionMapper.toEntity(userPermissionRequest, permission));

        syncPermissionService.syncUserPermissions(user.getGuid());

        log.info("Created user permission id={} for user={}", saved.getId(), user.getGuid());

        return userPermissionMapper.toResponse(user, saved);
    }

    @Transactional
    public UserPermissionResponse update(Long id, UserPermissionUpdateRequest userPermissionUpdateRequest) {
        UserPermission userPermission = userPermissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER_PERMISSION));

        User user = userRepository.findByGuid(userPermission.getUserGuid())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER));

        userPermission.setForMe(userPermissionUpdateRequest.forMe());
        userPermission.setForAll(userPermissionUpdateRequest.forAll());
        userPermission.setAllowed(userPermissionUpdateRequest.allowed());

        UserPermission saved = userPermissionRepository.save(userPermission);

        syncPermissionService.syncUserPermissions(userPermission.getUserGuid());

        log.info("Updated user permission id={}", id);

        return userPermissionMapper.toResponse(user, saved);
    }

    @Transactional
    public void delete(Long id) {
        UserPermission userPermission = userPermissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_USER_PERMISSION));

        userPermissionRepository.delete(userPermission);

        syncPermissionService.syncUserPermissions(userPermission.getUserGuid());

        log.info("Deleted user permission id={}", id);
    }

    @Transactional
    public void deleteAllByUserGuid(UUID userGuid) {
        if (!userRepository.existsByGuid(userGuid)) {
            throw new NotFoundException(NOT_FOUND_USER);
        }

        userPermissionRepository.deleteAllByUserGuid(userGuid);

        syncPermissionService.syncUserPermissions(userGuid);

        log.info("Deleted all user permissions for user={}", userGuid);
    }

    @Transactional
    public void syncUserPermissionsToRedis() {
        syncPermissionService.syncAllPermissionsToRedis();
    }
}
