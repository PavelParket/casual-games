package com.file_management_starter.exception;

import com.common_utils.exception.AbstractException;

public class S3OperationException extends AbstractException {

    public S3OperationException(String message) {
        super(message);
    }

    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
