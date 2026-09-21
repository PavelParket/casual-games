package com.casualgames.websockethub.domain.dto.response;

import lombok.Builder;

@Builder
public record RoomInviteResponse(

        UserResponse invitedUser
) {
}
