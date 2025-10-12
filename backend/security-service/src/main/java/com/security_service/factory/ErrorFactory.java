package com.security_service.factory;

import com.security_service.domain.dto.ErrorResponse;
import com.security_service.domain.enums.ErrorCode;
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
public class ErrorFactory implements Factory<ErrorResponse> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    @Override
    public ErrorResponse create(Object... args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Expected at least 4 arguments: HttpStatus, ErrorCode, String message, HttpServletRequest");
        }

        HttpStatus status = (HttpStatus) args[0];
        ErrorCode code = (ErrorCode) args[1];
        String message = args[2] != null ? (String) args[2] : code.getMessage();
        HttpServletRequest request = (HttpServletRequest) args[3];

        @SuppressWarnings("unchecked")
        Map<String, List<String>> details = args.length > 4 ? (Map<String, List<String>>) args[4] : null;

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
