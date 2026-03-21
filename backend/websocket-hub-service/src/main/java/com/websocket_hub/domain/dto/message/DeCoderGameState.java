package com.websocket_hub.domain.dto.message;

public record DeCoderGameState(
        Integer code,
        Integer exactMatch,
        Integer partialMatch
) {
}