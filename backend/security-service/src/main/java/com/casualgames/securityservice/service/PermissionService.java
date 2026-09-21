package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securityservice.domain.dto.admin.PermissionResponse;
import com.casualgames.securityservice.mapper.PermissionMapper;
import com.casualgames.securityservice.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_PERMISSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper;

    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    public PermissionResponse getById(Long id) {
        return permissionRepository.findById(id)
                .map(permissionMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_PERMISSION));
    }
}
