package com.casualgames.websockethub.domain.dto.client;

import com.casualgames.securitystarter.enums.Role;
import com.casualgames.securitystarter.enums.Status;
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
