package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TicTacToeTransactionInternalRequest(

        UUID roomId,

        RoomType roomType,

        List<PlayerBet> playerBets,

        UUID winner
) {
}
