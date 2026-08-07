package com.websocket_hub.domain.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record RoomInviteResponse(

        List<UUID> invitedUsers,

        Map<UUID, String> skippedUsers
) {
}
