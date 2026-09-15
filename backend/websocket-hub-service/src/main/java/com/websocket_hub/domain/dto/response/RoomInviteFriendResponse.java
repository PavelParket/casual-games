package com.websocket_hub.domain.dto.response;

import com.websocket_hub.domain.enums.RoomInviteFriendStatus;
import lombok.Builder;

@Builder
public record RoomInviteFriendResponse(

        UserResponse user,

        RoomInviteFriendStatus status
) {
}
