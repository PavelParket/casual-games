package com.websocket_hub.domain.dto.response;

import com.security_starter.enums.Status;
import lombok.Builder;

import java.util.UUID;

@Builder
public record PlayerResponse(

        UUID guid,

        String username,

        Status status,

        String linkProfilePicture,

        String linkProfilePictureMini
) {
}
