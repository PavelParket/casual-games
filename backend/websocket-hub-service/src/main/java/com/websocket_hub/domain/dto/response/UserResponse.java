package com.websocket_hub.domain.dto.response;

import com.security_starter.enums.Status;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(

        Long id,

        UUID guid,

        String username,

        Status status,

        String linkProfilePicture,

        String linkProfilePictureMini,

        Instant createdAt
) {
}
