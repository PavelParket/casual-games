package com.security_service.controller;

import com.security_service.domain.dto.admin.PermissionResponse;
import com.security_service.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public List<PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @GetMapping("/{id}")
    public PermissionResponse getById(@PathVariable Long id) {
        return permissionService.getById(id);
    }
}
