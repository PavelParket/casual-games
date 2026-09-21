package com.casualgames.securityservice.service;

import com.casualgames.securityservice.factory.TokenFactory;
import com.casualgames.securitystarter.enums.Status;
import com.casualgames.securitystarter.jwt.JwtClaimsExtractor;
import com.casualgames.securitystarter.jwt.JwtDecoder;
import com.casualgames.securitystarter.jwt.JwtProperties;
import com.casualgames.securitystarter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.casualgames.securityservice.config.ResourceMessageConstants.EXPIRED_TOKEN;

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
        if (jwtValidator.isExpired(token)) {
            throw new CredentialsExpiredException(EXPIRED_TOKEN);
        }

        return jwtClaimsExtractor.extractGuid(token);
    }

    public String extractEmail(String token) {
        if (jwtValidator.isExpired(token)) {
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
