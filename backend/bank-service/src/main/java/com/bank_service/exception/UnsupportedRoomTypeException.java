package com.bank_service.exception;

public class UnsupportedRoomTypeException extends RuntimeException {
    public UnsupportedRoomTypeException(String message) {
        super(message);
    }
}
