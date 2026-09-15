package com.websocket_hub.domain.dto.response;

import lombok.Builder;

@Builder
public record RoomInviteResponse(

        UserResponse invitedUser
) {
}
