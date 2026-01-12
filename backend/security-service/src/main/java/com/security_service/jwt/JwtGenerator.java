package com.security_service.jwt;

import com.security_starter.enums.Status;
import com.security_starter.jwt.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtGenerator {

    private final SecretKey key;

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID guid, String email, List<String> roles, Status status) {
        return Jwts.builder()
                .subject(guid.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("status", status)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID guid) {
        return Jwts.builder()
                .subject(guid.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.refreshExpiration()))
                .signWith(key)
                .compact();
    }

    public String generateSimpleAccessToken(UUID guid, String email, String role) {
        return generateAccessToken(guid, email, List.of(role), Status.DEFAULT);
    }
}
