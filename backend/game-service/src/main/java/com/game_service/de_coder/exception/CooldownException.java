package com.game_service.de_coder.exception;

import lombok.Getter;

@Getter
public class CooldownException extends RuntimeException {

    private final long remainingTimeMs;

    public CooldownException(long remainingTimeMs) {
        super("Rate limit exceeded. Remaining time: " + remainingTimeMs + "ms");
        this.remainingTimeMs = remainingTimeMs;
    }
}
