package com.security_service.mapper;

import com.security_service.domain.dto.admin.PermissionResponse;
import com.security_service.domain.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}
