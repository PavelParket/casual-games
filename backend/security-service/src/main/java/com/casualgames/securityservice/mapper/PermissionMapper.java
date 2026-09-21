package com.casualgames.securityservice.mapper;

import com.casualgames.securityservice.domain.dto.admin.PermissionResponse;
import com.casualgames.securityservice.domain.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}
