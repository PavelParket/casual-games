package com.casualgames.websockethub.domain.dto.response;

import com.casualgames.websockethub.domain.enums.RoomInviteFriendStatus;
import lombok.Builder;

@Builder
public record RoomInviteFriendResponse(

        UserResponse user,

        RoomInviteFriendStatus status
) {
}
