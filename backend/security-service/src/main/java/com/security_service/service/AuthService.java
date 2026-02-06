package com.security_service.service;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.LoginRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.mapper.AuthMapper;
import com.security_service.scheduler.PermissionSyncScheduler;
import com.security_starter.enums.Status;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final TokenService tokenService;

    private final CookieService cookieService;

    private final AuthMapper mapper;

    private final AuthenticationManager authenticationManager;

    private final PermissionSyncScheduler permissionSyncScheduler;

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

    public void logout(HttpServletResponse response) {
        cookieService.deleteRefreshToken(response);
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private AuthResponse generateTokens(UserResponse user, HttpServletResponse response) {
        String accessToken = tokenService.generateAccessToken(
                user.guid(),
                user.email(),
                List.of(user.role()),
                Status.DEFAULT
        );

        String refreshToken = tokenService.generateRefreshToken(user.guid());

        cookieService.addRefreshToken(response, refreshToken);

        return mapper.toResponse(user, accessToken);
    }

    public void manualSync(String string) {
        permissionSyncScheduler.manualSync();
    }
}
