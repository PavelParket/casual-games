package casualgames.userservice.dto;

import casualgames.userservice.enums.ErrorCode;
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
