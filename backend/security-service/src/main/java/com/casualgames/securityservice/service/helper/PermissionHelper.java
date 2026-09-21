package com.casualgames.securityservice.service.helper;

import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.enums.Operation;
import com.casualgames.securitystarter.enums.Permissions;
import com.casualgames.securitystarter.helper.PermissionContextHelper;
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
