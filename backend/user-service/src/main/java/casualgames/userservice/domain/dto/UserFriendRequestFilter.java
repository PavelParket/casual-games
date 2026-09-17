package casualgames.userservice.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserFriendRequestFilter(

        @Size(min = 3, message = "Username requires at least 3 characters")
        String username
) {
}
