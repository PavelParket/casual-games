package com.bank_service.factory;

import com.bank_service.domain.dto.ErrorResponse;
import com.bank_service.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    public ErrorResponse create(HttpStatus status,
                                ErrorCode code,
                                String message,
                                HttpServletRequest request,
                                Map<String, List<String>> details) {
        return ErrorResponse.builder()
                .timestamp(FORMATTER.format(Instant.now()))
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .details(details)
                .build();
    }
}
