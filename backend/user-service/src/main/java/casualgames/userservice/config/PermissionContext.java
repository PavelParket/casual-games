package casualgames.userservice.config;

import casualgames.userservice.enums.Role;
import casualgames.userservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionContext {

    private Role role;

    private Status status;

    private boolean isOwner;

    private UUID actorGuid;

    private UUID targetGuid;
}
