package com.common_utils.dto;

import com.common_utils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record ErrorResponse(

        ErrorCode errorCode,

        String message,

        int status,

        Instant timestamp,

        String path,

        Map<String, List<String>> details

) {

    private static final String VALIDATION_FAILED = "Validation failed";

    public static ErrorResponse of(ErrorCode errorCode,
                                   HttpStatus status,
                                   String message,
                                   Map<String, List<String>> details,
                                   HttpServletRequest request) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .details(details)
                .build();
    }

    public static ErrorResponse of(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, List<String>> details = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        String message = details.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(VALIDATION_FAILED);

        return ErrorResponse.of(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, details, request);
    }

    public static ErrorResponse of(HttpServletRequest request) {
        return ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                null,
                request);
    }
}
