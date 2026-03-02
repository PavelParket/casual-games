package com.websocket_hub.domain.dto.client;

import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import lombok.Builder;

import java.util.Collection;
import java.util.UUID;

@Builder
public record HorseRaceTransactionInternalRequest(

        UUID roomId,

        RoomType roomType,

        Integer winnerHorseIndex,

        Collection<HorseRacePlayerBet> playerBets

) {
}
