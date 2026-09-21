package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securityservice.domain.dto.admin.RolePermissionEntry;
import com.casualgames.securityservice.domain.dto.admin.RolePermissionResponse;
import com.casualgames.securityservice.domain.entity.Role;
import com.casualgames.securityservice.mapper.RolePermissionMapper;
import com.casualgames.securityservice.repository.RolePermissionRepository;
import com.casualgames.securityservice.repository.RoleRepository;
import com.casualgames.securityservice.repository.projection.RolePermissionProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_ROLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionService {

    private final RoleRepository roleRepository;

    private final RolePermissionRepository rolePermissionRepository;

    private final RolePermissionMapper rolePermissionMapper;

    public RolePermissionResponse getRoleWithPermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_ROLE, roleId)));

        List<RolePermissionEntry> rolePermissions = rolePermissionMapper.toEntries(rolePermissionRepository.findPermissionsByRole(role.getName()));

        return rolePermissionMapper.toResponse(role, rolePermissions);
    }

    public List<RolePermissionResponse> getAllRolesWithPermissions() {
        List<RolePermissionProjection> rolePermissions = rolePermissionRepository.findAllRolePermissions();

        Map<String, Long> roleIdMap = roleRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Role::getName,
                        Role::getId
                ));

        Map<String, List<RolePermissionEntry>> rolePermissionMap = new LinkedHashMap<>();

        rolePermissions.forEach(rolePermission ->
                rolePermissionMap.computeIfAbsent(rolePermission.getRoleName(), name -> new ArrayList<>())
                        .add(rolePermissionMapper.toEntry(rolePermission)));

        return rolePermissionMap.entrySet().stream()
                .map(rolePermission ->
                        rolePermissionMapper.toResponse(
                                roleIdMap.get(rolePermission.getKey()),
                                rolePermission.getKey(),
                                rolePermission.getValue()
                        )
                )
                .toList();
    }
}
