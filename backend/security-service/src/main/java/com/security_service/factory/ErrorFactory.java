package com.security_service.factory;

import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorFactory {

    public ErrorResponse create(ErrorCode errorCode,
                                String message,
                                HttpStatus httpStatus,
                                Map<String, List<String>> details,
                                HttpServletRequest httpServletRequest) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status(httpStatus.value())
                .timestamp(Instant.now())
                .path(httpServletRequest.getRequestURI())
                .details(details)
                .build();
    }
}
