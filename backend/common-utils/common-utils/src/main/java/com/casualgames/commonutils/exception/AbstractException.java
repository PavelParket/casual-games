package com.casualgames.commonutils.exception;

public abstract class AbstractException extends RuntimeException {

    protected AbstractException(String message) {
        super(message);
    }

    protected AbstractException(String message, Throwable cause) {
        super(message, cause);
    }
}
