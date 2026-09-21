package com.casualgames.securityservice.service;

import com.casualgames.commonutils.exception.NotFoundException;
import com.casualgames.securityservice.domain.dto.admin.RoleResponse;
import com.casualgames.securityservice.mapper.RoleMapper;
import com.casualgames.securityservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.casualgames.securityservice.config.ResourceMessageConstants.NOT_FOUND_ROLE;

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
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_ROLE, id)));
    }
}
