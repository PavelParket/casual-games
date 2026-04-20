package com.bank_service.service.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.helper.PermissionContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHelper {

    private final PermissionContextHelper permissionContextHelper;

    public PermissionContext getContext(UUID targetGuid) {
        return permissionContextHelper.createContextFromAuthentication(targetGuid);
    }

    public AuthenticationToken getToken() {
        return permissionContextHelper.getCurrentAuthentication();
    }

    public boolean hasPermissionForAll(Permissions permission, Operation operation) {
        return permissionContextHelper.hasPermissionForAll(permission, operation);
    }
}
