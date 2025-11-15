package com.security_service.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
import com.security_service.factory.ErrorFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorFactory factory;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorResponse error = factory.create(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTHENTICATION_ERROR,
                "Authentication required",
                request
        );

        log.warn("Unauthorized access attempt: IP={}, Method={}, URI={}, User-Agent={}, Exception={}",
                request.getRemoteAddr(), request.getMethod(),
                request.getRequestURI(), request.getHeader("User-Agent"),
                authException.getMessage()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}
