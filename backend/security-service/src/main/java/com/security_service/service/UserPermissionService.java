package com.security_service.service;

import com.security_service.domain.dto.admin.UserPermissionCreateRequest;
import com.security_service.domain.dto.admin.UserPermissionResponse;
import com.security_service.domain.dto.admin.UserPermissionUpdateRequest;
import com.security_service.domain.entity.Permission;
import com.security_service.domain.entity.User;
import com.security_service.domain.entity.UserPermission;
import com.security_service.exception.ConflictException;
import com.security_service.exception.NotFoundException;
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
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userPermissionRepository.findAllWithPermissionByUserGuid(userGuid).stream()
                .map(userPermission -> userPermissionMapper.toResponse(user, userPermission))
                .toList();
    }

    @Transactional
    public UserPermissionResponse create(UserPermissionCreateRequest userPermissionRequest) {
        User user = userRepository.findByGuid(userPermissionRequest.userGuid())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Permission permission = permissionRepository.findById(userPermissionRequest.permissionId())
                .orElseThrow(() -> new NotFoundException("Permission not found"));

        userPermissionRepository.findByUserGuidAndPermissionId(user.getGuid(), userPermissionRequest.permissionId())
                .ifPresent(userPermission -> {
                    throw new ConflictException("Permission already exists");
                });

        UserPermission saved = userPermissionRepository.save(userPermissionMapper.toEntity(userPermissionRequest, permission));

        syncPermissionService.syncUserPermissions(user.getGuid());

        log.info("Created user permission id={} for user={}", saved.getId(), user.getGuid());

        return userPermissionMapper.toResponse(user, saved);
    }

    @Transactional
    public UserPermissionResponse update(Long id, UserPermissionUpdateRequest userPermissionUpdateRequest) {
        UserPermission userPermission = userPermissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User permission not found"));

        User user = userRepository.findByGuid(userPermission.getUserGuid())
                .orElseThrow(() -> new NotFoundException("User not found"));

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
                .orElseThrow(() -> new NotFoundException("User permission not found"));

        userPermissionRepository.delete(userPermission);

        syncPermissionService.syncUserPermissions(userPermission.getUserGuid());

        log.info("Deleted user permission id={}", id);
    }

    @Transactional
    public void deleteAllByUserGuid(UUID userGuid) {
        if (!userRepository.existsByGuid(userGuid)) {
            throw new NotFoundException("User not found");
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
