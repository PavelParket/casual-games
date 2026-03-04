package com.websocket_hub.exception;

import com.websocket_hub.domain.dto.ErrorResponse;
import com.websocket_hub.domain.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRoomNotFound(RoomNotFoundException e) {
        log.warn("Room not found: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.ROOM_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RoomAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleRoomAlreadyExists(RoomAlreadyExistsException e) {
        log.warn("Room already exists: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.ROOM_ALREADY_EXISTS, e.getMessage());
    }

    @ExceptionHandler(RoomTypeNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRoomTypeNotFound(RoomTypeNotFoundException e) {
        log.warn("Room type not found: {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.ROOM_TYPE_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception e) {
        log.error("Unexpected error in hub HTTP layer", e);
        return ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
    }
}
