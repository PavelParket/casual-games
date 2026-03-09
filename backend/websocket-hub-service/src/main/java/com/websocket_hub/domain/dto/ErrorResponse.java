package com.websocket_hub.domain.dto;

import com.websocket_hub.domain.enums.ErrorCode;
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
}
