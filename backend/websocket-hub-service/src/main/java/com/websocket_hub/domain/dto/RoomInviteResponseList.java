package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.dto.response.UserResponse;
import lombok.Builder;
import org.springframework.data.web.PagedModel;

@Builder
public record RoomInviteResponseList(

        PagedModel<UserResponse> users
) {
}
