package com.security_service.service;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.UserResponse;
import com.security_service.exception.InvalidCredentialsException;
import com.security_service.factory.AccessTokenFactory;
import com.security_service.factory.RefreshTokenFactory;
import com.security_service.jwt.JwtProvider;
import com.security_service.mapper.AuthMapper;
import com.security_service.validator.TokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider provider;

    private final AccessTokenFactory accessFactory;

    private final RefreshTokenFactory refreshFactory;

    private final TokenValidator validator;

    public String generateAccessToken(String username, String email, String role) {
        return accessFactory.create(username, email, role);
    }

    public String generateRefreshToken(String username, String email, String role) {
        return refreshFactory.create(username, email, role);
    }

    public AuthResponse refresh(String token, UserService service, AuthMapper mapper) {
        if (validator.isExpired(token)) {
            throw new InvalidCredentialsException("Token is expired");
        }

        String email = provider.getEmail(token);

        UserResponse user = service.getByEmail(email);

        String accessToken = generateAccessToken(user.username(), user.email(), user.role());
        String refreshToken = generateRefreshToken(user.username(), user.email(), user.role());

        return mapper.toResponse(user, accessToken, refreshToken);
    }
}
