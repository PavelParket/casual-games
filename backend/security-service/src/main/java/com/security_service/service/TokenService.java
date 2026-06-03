package com.security_service.service;

import com.security_service.factory.TokenFactory;
import com.security_starter.enums.Status;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.jwt.JwtDecoder;
import com.security_starter.jwt.JwtProperties;
import com.security_starter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.EXPIRED_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtClaimsExtractor jwtClaimsExtractor;

    private final JwtDecoder jwtDecoder;

    private final JwtProperties jwtProperties;

    private final TokenFactory tokenFactory;

    private final JwtValidator jwtValidator;

    public String generateAccessToken(UUID guid, String email, List<String> roles, Status status, UUID sid) {
        return tokenFactory.createAccessToken(guid, email, roles, status, sid);
    }

    public String generateRefreshToken(UUID guid, UUID sid) {
        return tokenFactory.createRefreshToken(guid, sid);
    }

    public UUID extractGuid(String token) {
        if (jwtValidator.isExpiredToken(token)) {
            throw new CredentialsExpiredException(EXPIRED_TOKEN);
        }

        return jwtClaimsExtractor.extractGuid(token);
    }

    public String extractEmail(String token) {
        if (jwtValidator.isExpiredToken(token)) {
            throw new CredentialsExpiredException(EXPIRED_TOKEN);
        }

        return jwtClaimsExtractor.extractEmail(token);
    }

    public UUID extractSid(String token) {
        return jwtClaimsExtractor.extractSid(token);
    }

    public Duration extractExpiration(String token) {
        try {
            Date expiration = jwtDecoder.decode(token).getExpiration();

            if (expiration == null) {
                return Duration.ofSeconds(jwtProperties.accessExpiration());
            }

            long remainingSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Duration.ofSeconds(Math.max(remainingSeconds, 0));
        } catch (Exception e) {
            log.warn("Failed to extract TTL from token, using default: {}", e.getMessage());

            return Duration.ofSeconds(jwtProperties.accessExpiration());
        }
    }
}
