package com.security_service.exception;

import com.common_utils.exception.AbstractException;

public class InvalidTokenException extends AbstractException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
