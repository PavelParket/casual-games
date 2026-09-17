package com.notifications.service.helper;

import com.security_starter.config.PermissionContext;
import com.security_starter.helper.PermissionContextHelper;
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
