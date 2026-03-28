package com.websocket_hub.domain.dto.message;

public record DeCoderGameState(
        String code,
        Integer exactMatch,
        Integer partialMatch
) {
}