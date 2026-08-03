package casualgames.userservice.service.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.helper.PermissionContextHelper;
import com.security_starter.validator.PermissionValidator;
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
