package com.common_utils.dto;

import com.common_utils.enums.ErrorCode;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record ErrorResponse(

        ErrorCode errorCode,

        String message,

        int status,

        Instant timestamp,

        String path,

        Map<String, List<String>> details

) {

    public static ErrorResponse of(ErrorCode errorCode,
                                   String message,
                                   String path,
                                   Map<String, List<String>> details) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status(errorCode.getHttpStatus().value())
                .timestamp(Instant.now())
                .path(path)
                .details(details)
                .build();
    }

    public static ErrorResponse of(String path) {
        return ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                path,
                null
        );
    }
}
