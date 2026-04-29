package com.security_service.service;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.LoginRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.mapper.AuthMapper;
import com.security_service.repository.BlockedTokenRedisRepository;
import com.security_starter.enums.Status;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final static String BEARER_PREFIX = "Bearer ";

    private final UserService userService;

    private final TokenService tokenService;

    private final CookieService cookieService;

    private final AuthMapper mapper;

    private final AuthenticationManager authenticationManager;

    private final BlockedTokenRedisRepository blockedTokenRedisRepository;

    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        UserResponse user = userService.create(request);

        return generateTokens(user, response);
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticate(request.email(), request.password());

        UserResponse user = userService.getByEmail(request.email());

        return generateTokens(user, response);
    }

    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieService.extractRefreshToken(request);

        UserResponse user = userService.getByGuid(tokenService.extractGuid(token));

        return generateTokens(user, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.deleteRefreshToken(response);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }

        String accessToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            String email = tokenService.extractEmail(accessToken);
            Duration ttl = tokenService.extractExpiration(accessToken);
            blockedTokenRedisRepository.block(email, accessToken, ttl);
        } catch (Exception e) {
            log.warn("Failed to block token on logout: {}", e.getMessage());
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private AuthResponse generateTokens(UserResponse user, HttpServletResponse response) {
        String accessToken = tokenService.generateAccessToken(
                user.getGuid(),
                user.getEmail(),
                List.of(user.getRole().toString()),
                Status.DEFAULT
        );

        String refreshToken = tokenService.generateRefreshToken(user.getGuid());

        cookieService.addRefreshToken(response, refreshToken);

        return mapper.toResponse(user, accessToken);
    }
}
