package com.security_starter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtDecoder {

    private final JwtParser jwtParser;

    public JwtDecoder(JwtKeyProvider jwtKeyProvider) {
        this.jwtParser = Jwts.parser()
                .verifyWith(jwtKeyProvider.getSigningKey())
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
