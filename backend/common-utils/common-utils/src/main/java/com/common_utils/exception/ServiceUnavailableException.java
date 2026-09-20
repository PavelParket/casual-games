package com.common_utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends AbstractException {

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
