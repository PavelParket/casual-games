package com.casualgames.userservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class InvalidAttachmentTypeException extends AbstractException {

    public InvalidAttachmentTypeException(String message) {
        super(message);
    }
}
