package com.security_service.jwt;


import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final SecretKey key;

    private final JwtParser parser;

    public String generateToken(UUID guid, Long expiration) {
        return Jwts.builder()
                .subject(guid.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public UUID getGuid(String token) {
        return UUID.fromString(parser.parseSignedClaims(token).getPayload().getSubject());
    }
}
