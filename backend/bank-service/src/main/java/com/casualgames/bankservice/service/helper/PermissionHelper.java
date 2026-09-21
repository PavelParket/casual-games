package com.casualgames.bankservice.service.helper;

import com.casualgames.securitystarter.config.AuthenticationToken;
import com.casualgames.securitystarter.config.PermissionContext;
import com.casualgames.securitystarter.helper.PermissionContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHelper {

    private final PermissionContextHelper permissionContextHelper;

    public PermissionContext getContext(UUID targetGuid, AuthenticationToken authenticationToken) {
        return permissionContextHelper.createContextFromAuthentication(authenticationToken, targetGuid);
    }
}
