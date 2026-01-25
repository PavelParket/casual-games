package com.security_starter.validator;

import com.security_starter.jwt.JwtDecoder;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

    private final JwtDecoder jwtDecoder;

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Claims claims = jwtDecoder.decode(token);
            return !isExpiredClaims(claims);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isExpiredToken(String token) {
        try {
            Claims claims = jwtDecoder.decode(token);
            return isExpiredClaims(claims);
        } catch (Exception e) {
            log.debug("Token expiration check failed: {}", e.getMessage());
            return true;
        }
    }

    private boolean isExpiredClaims(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public boolean isValidAndNotExpired(String token) {
        return isValid(token) && !isExpiredToken(token);
    }
}
