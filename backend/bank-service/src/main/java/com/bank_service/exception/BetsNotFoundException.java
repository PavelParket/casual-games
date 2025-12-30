package com.bank_service.exception;

public class BetsNotFoundException extends RuntimeException {
    public BetsNotFoundException(String message) {
        super(message);
    }
}
