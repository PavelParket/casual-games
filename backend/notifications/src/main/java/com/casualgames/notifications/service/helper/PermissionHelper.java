package com.casualgames.notifications.service.helper;

import com.casualgames.securitystarter.config.PermissionContext;
import com.casualgames.securitystarter.helper.PermissionContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionHelper {

    private final PermissionContextHelper permissionContextHelper;

    public PermissionContext getContext(UUID targetGuid) {
        return permissionContextHelper.createContextFromAuthentication(targetGuid);
    }
}
