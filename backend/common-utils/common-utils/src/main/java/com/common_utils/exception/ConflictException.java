package com.common_utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends AbstractException {

    public ConflictException(String message) {
        super(message);
    }
}
