package casualgames.userservice.domain.dto;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record FriendRequestResponseList(

        PagedModel<FriendRequestResponse> incomingFriendRequests,

        PagedModel<FriendRequestResponse> outgoingFriendRequests,

        long incomingCount,

        long outgoingCount
) {
}
