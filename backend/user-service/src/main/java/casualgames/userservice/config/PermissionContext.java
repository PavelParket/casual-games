package casualgames.userservice.config;

import casualgames.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionContext {

    private Role role;

    private boolean isOwner;
}
