package com.casualgames.securityservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class SessionRevokedException extends AbstractException {

    public SessionRevokedException(String message) {
        super(message);
    }
}
