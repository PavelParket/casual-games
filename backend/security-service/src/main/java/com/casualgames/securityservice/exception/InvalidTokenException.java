package com.casualgames.securityservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class InvalidTokenException extends AbstractException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
