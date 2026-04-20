package com.security_service.service;

import com.security_service.domain.dto.admin.RoleResponse;
import com.security_service.exception.NotFoundException;
import com.security_service.mapper.RoleMapper;
import com.security_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    private final RoleMapper roleMapper;

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    public RoleResponse getById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Role not found: " + id));
    }
}
