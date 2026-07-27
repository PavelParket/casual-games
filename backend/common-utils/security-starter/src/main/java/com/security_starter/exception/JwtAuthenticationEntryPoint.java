package com.security_starter.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.security_starter.config.ResourceMessageConstants.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("Unauthorized access attempt: IP={}, Method={}, URI={}, User-Agent={}, Exception={}",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("User-Agent"),
                authException.getMessage()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED,
                null,
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
