package com.security_service.mapper;

import com.security_service.domain.dto.admin.UserPermissionCreateRequest;
import com.security_service.domain.dto.admin.UserPermissionResponse;
import com.security_service.domain.entity.Permission;
import com.security_service.domain.entity.User;
import com.security_service.domain.entity.UserPermission;
import com.security_service.repository.projection.UserPermissionProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPermissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserPermission toEntity(UserPermissionCreateRequest userPermissionCreateRequest, Permission permission);

    @Mapping(target = "id", source = "userPermission.id")
    @Mapping(target = "permissionId", source = "userPermission.permission.id")
    @Mapping(target = "attribute", source = "userPermission.permission.attribute")
    @Mapping(target = "operation", source = "userPermission.permission.operation")
    UserPermissionResponse toResponse(User user, UserPermission userPermission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissionId", ignore = true)
    @Mapping(target = "userGuid", expression = "java(user.getGuid())")
    UserPermissionResponse toResponse(User user, UserPermissionProjection userPermissionProjection);
}
