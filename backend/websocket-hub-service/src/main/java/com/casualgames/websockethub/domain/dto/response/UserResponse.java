package com.casualgames.websockethub.domain.dto.response;

import com.casualgames.securitystarter.enums.Status;
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
