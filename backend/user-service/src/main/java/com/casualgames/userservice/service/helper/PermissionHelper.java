package com.casualgames.userservice.service.helper;

import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.config.PermissionContext;
import com.casualgames.securitystarter.enums.Operation;
import com.casualgames.securitystarter.enums.Permissions;
import com.casualgames.securitystarter.helper.PermissionContextHelper;
import com.casualgames.securitystarter.validator.PermissionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHelper {

    private final PermissionContextHelper permissionContextHelper;

    private final PermissionValidator permissionValidator;

    public PermissionContext getContext(UUID targetGuid, AuthenticationToken authenticationToken) {
        return permissionContextHelper.createContextFromAuthentication(authenticationToken, targetGuid);
    }

    public PermissionContext getContext(UUID targetGuid) {
        return permissionContextHelper.createContextFromAuthentication(targetGuid);
    }

    public boolean hasAccess(Permissions permission, Operation operation, UUID targetGuid, AuthenticationToken token) {
        return permissionValidator.hasAccess(permission, operation, getContext(targetGuid, token), token);
    }
}
