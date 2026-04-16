package com.security_service.controller;

import com.security_service.domain.dto.admin.RolePermissionResponse;
import com.security_service.domain.dto.admin.RoleResponse;
import com.security_service.service.RolePermissionService;
import com.security_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    private final RolePermissionService rolePermissionService;

    @GetMapping
    public List<RoleResponse> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @GetMapping("/permissions/{roleId}")
    public RolePermissionResponse getRoleWithPermissions(@PathVariable Long roleId) {
        return rolePermissionService.getRoleWithPermissions(roleId);
    }

    @GetMapping("/permissions")
    public List<RolePermissionResponse> getAllRolesWithPermissions() {
        return rolePermissionService.getAllRolesWithPermissions();
    }
}
