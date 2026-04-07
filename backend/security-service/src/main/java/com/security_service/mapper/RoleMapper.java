package com.security_service.mapper;

import com.security_service.domain.dto.admin.RoleResponse;
import com.security_service.domain.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);
}
