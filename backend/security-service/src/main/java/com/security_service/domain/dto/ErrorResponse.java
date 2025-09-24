package com.security_service.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.security_service.domain.enums.ErrorCode;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String timestamp,

        int status,

        String error,

        ErrorCode code,

        String message,

        String path,

        Map<String, List<String>> details
) {
}
