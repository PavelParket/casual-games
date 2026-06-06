package com.security_service.exception;

import com.common_utils.exception.AbstractException;

public class SessionRevokedException extends AbstractException {

    public SessionRevokedException(String message) {
        super(message);
    }
}
