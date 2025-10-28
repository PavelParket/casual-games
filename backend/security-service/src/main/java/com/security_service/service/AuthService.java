package com.security_service.service;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.LoginRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final TokenService tokenService;

    private final AuthMapper mapper;

    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        UserResponse user = userService.create(request);

        //String accessToken = tokenService.generateAccessToken(user.username(), user.email(), user.role());
        //String refreshToken = tokenService.generateRefreshToken(user.username(), user.email(), user.role());

        //return mapper.toResponse(user, accessToken, refreshToken);

        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserResponse user = userService.getByEmail(request.email());

        authenticate(request.email(), request.password());

        return generateTokens(user);

        //String accessToken = tokenService.generateAccessToken(user.username(), user.email(), user.role());
        //String refreshToken = tokenService.generateRefreshToken(user.username(), user.email(), user.role());

        //return mapper.toResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refresh(String token) {
        return tokenService.refresh(token, userService, mapper);
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private AuthResponse generateTokens(UserResponse user) {
        String accessToken = tokenService.generateAccessToken(user.username(), user.email(), user.role());
        String refreshToken = tokenService.generateRefreshToken(user.username(), user.email(), user.role());

        return mapper.toResponse(user, accessToken, refreshToken);
    }
}
