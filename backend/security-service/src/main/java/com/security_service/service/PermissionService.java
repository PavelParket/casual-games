package com.security_service.service;

import com.common_utils.exception.NotFoundException;
import com.security_service.domain.dto.admin.PermissionResponse;
import com.security_service.mapper.PermissionMapper;
import com.security_service.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.security_service.config.ResourceMessageConstants.NOT_FOUND_PERMISSION;

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
