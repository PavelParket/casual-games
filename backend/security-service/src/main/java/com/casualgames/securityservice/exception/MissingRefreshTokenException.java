package com.casualgames.securityservice.exception;

import com.casualgames.commonutils.exception.AbstractException;

import static com.casualgames.securityservice.config.ResourceMessageConstants.MISSING_REFRESH_TOKEN;

public class MissingRefreshTokenException extends AbstractException {

    public MissingRefreshTokenException() {
        super(MISSING_REFRESH_TOKEN);
    }
}
