package casualgames.userservice.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionContext {

    private boolean isAdmin;

    private boolean isOwner;

    public boolean canSeePrivateFields() {
        return isAdmin || isOwner;
    }

    public boolean canUpdateAnyProfile() {
        return isAdmin;
    }

    public boolean canUpdateOwnProfile() {
        return isOwner;
    }
}
