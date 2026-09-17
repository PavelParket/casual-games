package casualgames.userservice.domain.dto;

import casualgames.userservice.domain.enums.FriendRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FriendRequestRequest(

        @NotNull(message = "Friend request id is required")
        Long id,

        @NotNull(message = "Friend request status is required")
        FriendRequestStatus status
) {
}
