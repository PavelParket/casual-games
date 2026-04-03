package com.security_service.service;

import com.security_service.factory.TokenFactory;
import com.security_starter.enums.Status;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.security_service.config.ResourceMessageConstants.EXPIRED_TOKEN;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtClaimsExtractor jwtClaimsExtractor;

    private final TokenFactory tokenFactory;

    private final JwtValidator jwtValidator;

    public String generateAccessToken(UUID guid, String email, List<String> roles, Status status) {
        return tokenFactory.createAccessToken(guid, email, roles, status);
    }

    public String generateRefreshToken(UUID guid) {
        return tokenFactory.createRefreshToken(guid);
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
}
