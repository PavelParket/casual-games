package com.security_starter.jwt;

import com.security_starter.enums.Status;
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
