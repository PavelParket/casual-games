package com.security_service.service;

import com.common_utils.exception.ConflictException;
import com.common_utils.exception.NotFoundException;
import com.security_service.domain.dto.admin.UserPermissionCreateRequest;
import com.security_service.domain.dto.admin.UserPermissionResponse;
import com.security_service.domain.dto.admin.UserPermissionUpdateRequest;
import com.security_service.domain.entity.Permission;
import com.security_service.domain.entity.User;
import com.security_service.domain.entity.UserPermission;
import com.security_service.mapper.UserPermissionMapper;
import com.security_service.repository.PermissionRepository;
import com.security_service.repository.UserPermissionRepository;
import com.security_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.CONFLICT_USER_PERMISSION;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_PERMISSION;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER;
import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_USER_PERMISSION;

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
