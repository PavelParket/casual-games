package com.security_service.exception;

import com.common_utils.exception.AbstractException;

import static com.security_service.config.ResourceMessageConstants.MISSING_REFRESH_TOKEN;

public class MissingRefreshTokenException extends AbstractException {

    public MissingRefreshTokenException() {
        super(MISSING_REFRESH_TOKEN);
    }
}
