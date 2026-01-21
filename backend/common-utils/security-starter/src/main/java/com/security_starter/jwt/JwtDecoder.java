package com.security_starter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtDecoder {

    private final JwtParser jwtParser;

    public JwtDecoder(JwtProperties jwtProperties) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
                .verifyWith(key)
                .build();
    }

    public Claims decode(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            throw new JwtException("Invalid JWT token", e);
        }
    }

    public boolean isValid(String token) {
        try {
            decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
