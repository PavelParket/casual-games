package casualgames.userservice.factory;

import casualgames.userservice.config.PermissionContext;
import casualgames.userservice.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionContextFactory {

    public static PermissionContext create(Role role, Boolean isOwner, UUID actorGuid, UUID targetGuid) {
        return PermissionContext.builder()
                .role(role)
                .isOwner(isOwner)
                .actorGuid(actorGuid)
                .targetGuid(targetGuid)
                .build();
    }
}
