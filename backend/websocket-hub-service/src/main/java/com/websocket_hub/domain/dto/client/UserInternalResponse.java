package com.websocket_hub.domain.dto.client;

import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record UserInternalResponse(

        UUID guid,

        String username,

        String email,

        BigDecimal balance,

        Role role,

        Status status,

        String linkProfilePicture,

        String linkProfilePictureMini
) {
}
