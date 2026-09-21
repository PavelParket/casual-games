package com.casualgames.securityservice.mapper;

import com.casualgames.securityservice.domain.dto.admin.RolePermissionEntry;
import com.casualgames.securityservice.domain.dto.admin.RolePermissionResponse;
import com.casualgames.securityservice.domain.entity.Role;
import com.casualgames.securityservice.repository.projection.PermissionProjection;
import com.casualgames.securityservice.repository.projection.RolePermissionProjection;
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
