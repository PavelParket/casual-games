package casualgames.userservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND("Not Found"),
    CONFLICT("Conflict"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred. Please try again");

    private final String message;
}
