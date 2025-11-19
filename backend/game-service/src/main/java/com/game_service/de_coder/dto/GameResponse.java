package com.game_service.de_coder.dto;

import com.game_service.de_coder.enums.MessageType;

import java.util.Set;

public record GameResponse(
        MessageType type,

        String roomName,

        Integer code,

        String player,

        Set<String> players,

        String winner,

        String message
) {
}
