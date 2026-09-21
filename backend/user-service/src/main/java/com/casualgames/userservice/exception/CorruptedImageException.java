package com.casualgames.userservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class CorruptedImageException extends AbstractException {

    public CorruptedImageException(String message) {
        super(message);
    }
}
