package com.security_service.jwt;


import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final SecretKey key;

    private final JwtParser parser;

    public String generateToken(String username, String email, String role, Long expiration) {
        Map<String, String> claims = new HashMap<>() {{
            put("role", role);
            put("username", username);
        }};

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String getEmail(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    public String getUsername(String token) {
        return parser.parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return parser.parseSignedClaims(token).getPayload().get("role", String.class);
    }
}
