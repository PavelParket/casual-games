package com.security_service.service.helper;

import com.security_service.domain.dto.UserResponse;
import com.security_service.exception.ForbiddenException;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.helper.PermissionContextHelper;
import com.security_starter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionHelper {

    private final PermissionValidator permissionValidator;

    private final PermissionContextHelper permissionContextHelper;

    public void applyReadPermissions(UserResponse response, UUID targetGuid) {
        AuthenticationToken token = permissionContextHelper.getCurrentAuthentication();
        PermissionContext context = permissionContextHelper.createContextFromAuthentication(targetGuid);
        permissionValidator.readObject(response, context, token);
    }

    public void checkUpdatePermission(UUID targetGuid) {
        if (!permissionContextHelper.hasPermission(Permissions.USER, Operation.UPDATE, targetGuid)) {
            throw new ForbiddenException("No permission to update this user");
        }
    }

    public void checkDeletePermission(UUID targetGuid) {
        if (!permissionContextHelper.hasPermission(Permissions.USER, Operation.DELETE, targetGuid)) {
            throw new ForbiddenException("No permission to delete this user");
        }
    }

    public void checkRoleUpdatePermission() {
        if (!permissionContextHelper.hasPermissionForAll(Permissions.ROLE, Operation.UPDATE)) {
            throw new ForbiddenException("No permission to change user role");
        }
    }
}
