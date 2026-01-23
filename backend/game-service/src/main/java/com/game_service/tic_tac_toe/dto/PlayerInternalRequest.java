package com.game_service.tic_tac_toe.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PlayerInternalRequest(

        UUID guid,

        String username,

        String email
) {
}
