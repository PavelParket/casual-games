package com.security_service.exception;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorFactory factory;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ErrorResponse error = factory.create(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "You do not have permission to access this resource",
                request
        );

        log.warn("Access denied: IP={}, Method={}, URI={}, User-Agent={}, Exception={}",
                request.getRemoteAddr(), request.getMethod(),
                request.getRequestURI(), request.getHeader("User-Agent"),
                accessDeniedException.getMessage()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}
