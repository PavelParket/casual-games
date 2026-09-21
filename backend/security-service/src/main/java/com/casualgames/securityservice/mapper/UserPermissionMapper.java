package com.casualgames.securityservice.mapper;

import com.casualgames.securityservice.domain.dto.admin.UserPermissionCreateRequest;
import com.casualgames.securityservice.domain.dto.admin.UserPermissionResponse;
import com.casualgames.securityservice.domain.entity.Permission;
import com.casualgames.securityservice.domain.entity.User;
import com.casualgames.securityservice.domain.entity.UserPermission;
import com.casualgames.securityservice.repository.projection.UserPermissionProjection;
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
