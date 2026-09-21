package com.casualgames.websockethub.domain.entity;

public record DeCoderGameState(
        String code,
        Integer exactMatch,
        Integer partialMatch
) {
}