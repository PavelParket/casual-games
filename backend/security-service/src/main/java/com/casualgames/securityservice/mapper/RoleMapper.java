package com.casualgames.securityservice.mapper;

import com.casualgames.securityservice.domain.dto.admin.RoleResponse;
import com.casualgames.securityservice.domain.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);
}
