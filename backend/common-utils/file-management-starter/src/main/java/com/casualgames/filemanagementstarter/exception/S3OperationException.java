package com.casualgames.filemanagementstarter.exception;

import com.casualgames.commonutils.exception.AbstractException;

public class S3OperationException extends AbstractException {

    public S3OperationException(String message) {
        super(message);
    }

    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
