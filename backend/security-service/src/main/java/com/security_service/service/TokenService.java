package com.security_service.service;

import com.security_service.exception.InvalidCredentialsException;
import com.security_service.factory.AccessTokenFactory;
import com.security_service.factory.RefreshTokenFactory;
import com.security_starter.enums.Status;
import com.security_starter.jwt.JwtClaimsExtractor;
import com.security_starter.validator.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtClaimsExtractor claimsExtractor;

    private final AccessTokenFactory accessFactory;

    private final RefreshTokenFactory refreshFactory;

    private final JwtValidator validator;

    public String generateAccessToken(UUID guid, String email, List<String> roles, Status status) {
        return accessFactory.create(guid, email, roles, status);
    }

    public String generateRefreshToken(UUID guid) {
        return refreshFactory.create(guid);
    }

    public UUID extractGuid(String token) {
        if (validator.isExpiredToken(token)) {
            throw new InvalidCredentialsException("Token is expired");
        }

        return claimsExtractor.extractGuid(token);
    }

    public String extractEmail(String token) {
        if (validator.isExpiredToken(token)) {
            throw new InvalidCredentialsException("Token is expired");
        }

        return claimsExtractor.extractEmail(token);
    }
}
