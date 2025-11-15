package com.security_service.controller;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.LoginRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.service.AuthService;
import com.security_service.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    private final CookieService cookieService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResponse authResponse = service.register(request);

        cookieService.addRefreshToken(response, authResponse.refreshToken());

        return authResponse;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = service.login(request);

        cookieService.addRefreshToken(response, authResponse.refreshToken());

        return authResponse;
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = service.refresh(cookieService.extractRefreshToken(request));

        cookieService.addRefreshToken(response, authResponse.refreshToken());

        return authResponse;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        cookieService.deleteRefreshToken(response);
    }
}
