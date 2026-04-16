package com.bank_service.factory;

import com.bank_service.domain.dto.ErrorResponse;
import com.bank_service.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    public ErrorResponse create(ErrorCode code,
                                String message,
                                HttpStatus status,
                                HttpServletRequest request,
                                Map<String, List<String>> details) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .details(details)
                .build();
    }
}
