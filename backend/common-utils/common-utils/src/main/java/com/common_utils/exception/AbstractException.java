package com.common_utils.exception;

public abstract class AbstractException extends RuntimeException {

    protected AbstractException(String message) {
        super(message);
    }
}
