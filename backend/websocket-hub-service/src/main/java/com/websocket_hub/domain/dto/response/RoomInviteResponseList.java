package com.websocket_hub.domain.dto.response;

import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record RoomInviteResponseList(

        PagedModel<RoomInviteFriendResponse> friends
) {
}
