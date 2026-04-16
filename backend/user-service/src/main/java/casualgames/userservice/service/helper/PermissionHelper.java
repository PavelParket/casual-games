package casualgames.userservice.service.helper;

import com.security_starter.config.AuthenticationToken;
import com.security_starter.config.PermissionContext;
import com.security_starter.helper.PermissionContextHelper;
import com.security_starter.provider.PermissionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHelper {

    private final PermissionProvider permissionProvider;

    private final PermissionContextHelper permissionContextHelper;

    public PermissionContext getContext(UUID targetGuid) {
        return permissionContextHelper.createContextFromAuthentication(targetGuid);
    }

    public AuthenticationToken getToken() {
        return permissionProvider.getToken();
    }
}
