package com.security_service.service;

import com.security_service.exception.InvalidCredentialsException;
import com.security_service.factory.AccessTokenFactory;
import com.security_service.factory.RefreshTokenFactory;
import com.security_service.jwt.JwtProvider;
import com.security_service.validator.TokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider provider;

    private final AccessTokenFactory accessFactory;

    private final RefreshTokenFactory refreshFactory;

    private final TokenValidator validator;

    public String generateAccessToken(UUID guid) {
        return accessFactory.create(guid);
    }

    public String generateRefreshToken(UUID guid) {
        return refreshFactory.create(guid);
    }

    public UUID extractGuid(String token) {
        if (validator.isExpired(token)) {
            throw new InvalidCredentialsException("Token is expired");
        }

        return provider.getGuid(token);
    }
}
