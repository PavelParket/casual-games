package com.websocket_hub.domain.entity;

public record DeCoderGameState(
        String code,
        Integer exactMatch,
        Integer partialMatch
) {
}