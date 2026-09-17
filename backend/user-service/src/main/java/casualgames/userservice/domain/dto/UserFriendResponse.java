package casualgames.userservice.domain.dto;

import casualgames.userservice.domain.enums.FriendshipStatus;
import com.security_starter.enums.Status;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserFriendResponse(

        UUID guid,

        String username,

        Status status,

        String linkProfilePicture,

        String linkProfilePictureMini,

        FriendshipStatus friendshipStatus
) {
}
