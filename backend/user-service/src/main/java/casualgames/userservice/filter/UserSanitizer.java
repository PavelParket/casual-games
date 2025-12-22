package casualgames.userservice.filter;

import casualgames.userservice.config.PermissionContext;
import casualgames.userservice.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSanitizer {

    public void sanitize(UserResponseDto response, PermissionContext context) {
        if (!context.canSeePrivateFields()) {
            response.setEmail(null);
            response.setBalance(null);
            response.setRole(null);
            response.setCreatedAt(null);
        }
    }
}
