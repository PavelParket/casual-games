package com.security_starter.jwt;

import com.security_starter.enums.Status;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtClaimsExtractor {

    private final JwtDecoder jwtDecoder;

    public TokenClaims extractAll(String token) {
        Claims claims = jwtDecoder.decode(token);

        return new TokenClaims(
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get("sid", String.class)),
                claims.get("email", String.class),
                Status.valueOf(claims.get("status", String.class)),
                claims.get("roles", List.class) != null
                        ? new HashSet<>(claims.get("roles", List.class)) : Set.of()
        );
    }

    public UUID extractGuid(String token) {
        Claims claims = jwtDecoder.decode(token);
        String subject = claims.getSubject();
        return UUID.fromString(subject);
    }

    public String extractEmail(String token) {
        Claims claims = jwtDecoder.decode(token);
        return claims.get("email", String.class);
    }

    public Set<String> extractRoles(String token) {
        Claims claims = jwtDecoder.decode(token);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return roles != null ? new HashSet<>(roles) : Set.of();
    }

    public Status extractStatus(String token) {
        Claims claims = jwtDecoder.decode(token);
        String statusStr = claims.get("status", String.class);
        try {
            return statusStr != null ? Status.valueOf(statusStr) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status in token: {}", statusStr);
            return null;
        }
    }

    public UUID extractSid(String token) {
        Claims claims = jwtDecoder.decode(token);
        String sidStr = claims.get("sid", String.class);
        return UUID.fromString(sidStr);
    }

    public Claims extractClaims(String token) {
        return jwtDecoder.decode(token);
    }
}
