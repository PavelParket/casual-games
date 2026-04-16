package com.bank_service.domain.dto;

import com.bank_service.domain.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        ErrorCode code,

        String message,

        int status,

        Instant timestamp,

        String path,

        Map<String, List<String>> details
) {
}
