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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.security_starter.config.ResourceMessageConstants.FORBIDDEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied: IP={}, Method={}, URI={}, User-Agent={}, Exception={}",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("User-Agent"),
                accessDeniedException.getMessage()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.FORBIDDEN,
                HttpStatus.FORBIDDEN,
                FORBIDDEN,
                null,
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
