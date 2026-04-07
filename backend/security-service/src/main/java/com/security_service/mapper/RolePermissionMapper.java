package com.security_service.mapper;

import com.security_service.domain.dto.admin.RolePermissionEntry;
import com.security_service.domain.dto.admin.RolePermissionResponse;
import com.security_service.domain.entity.Role;
import com.security_service.repository.projection.PermissionProjection;
import com.security_service.repository.projection.RolePermissionProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RolePermissionMapper {

    @Mapping(target = "permissionId", ignore = true)
    RolePermissionEntry toEntry(PermissionProjection permissionProjection);

    @Mapping(target = "permissionId", ignore = true)
    RolePermissionEntry toEntry(RolePermissionProjection rolePermissionProjection);

    List<RolePermissionEntry> toEntries(List<PermissionProjection> permissionProjections);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    RolePermissionResponse toResponse(Role role, List<RolePermissionEntry> permissions);

    RolePermissionResponse toResponse(Long roleId, String roleName, List<RolePermissionEntry> permissions);
}
