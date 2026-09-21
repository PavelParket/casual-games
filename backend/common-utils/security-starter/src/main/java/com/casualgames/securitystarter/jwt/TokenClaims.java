package com.casualgames.securitystarter.jwt;

import com.casualgames.securitystarter.enums.Status;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record TokenClaims(

        UUID guid,

        UUID sid,

        String email,

        Status status,

        Set<String> roles
) {
}
