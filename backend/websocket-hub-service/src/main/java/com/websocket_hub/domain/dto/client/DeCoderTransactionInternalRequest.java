package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeCoderTransactionInternalRequest(

        UUID roomId,

        RoomType roomType,

        PlayerBet playerBet,

        UUID winner
) {
}