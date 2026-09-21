package com.casualgames.userservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class InvalidImageDimensionsException extends AbstractException {

    public InvalidImageDimensionsException(String message) {
        super(message);
    }
}
