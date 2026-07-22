package com.security_service.service.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.helper.PermissionContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionHelper {

    private final PermissionContextHelper permissionContextHelper;

    public boolean hasPermission(Permissions permission, Operation operation, UUID targetUserId, AuthenticationToken token) {
        return permissionContextHelper.hasPermission(token, permission, operation, targetUserId);
    }
}
