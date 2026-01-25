package com.security_starter.jwt;

import com.security_starter.enums.Role;
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

    public Role extractRole(String token) {
        Claims claims = jwtDecoder.decode(token);
        String roleStr = claims.get("role", String.class);
        try {
            return roleStr != null ? Role.valueOf(roleStr) : null;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role in token: {}", roleStr);
            return null;
        }
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

    public Claims extractClaims(String token) {
        return jwtDecoder.decode(token);
    }
}
