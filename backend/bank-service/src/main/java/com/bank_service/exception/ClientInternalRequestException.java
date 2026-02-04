package com.bank_service.exception;

public class ClientInternalRequestException extends RuntimeException {
    public ClientInternalRequestException(String message) {
        super(message);
    }
}
