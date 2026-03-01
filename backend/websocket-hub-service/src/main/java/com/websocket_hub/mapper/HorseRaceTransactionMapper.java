package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.HorseRaceTransactionInternalRequest;
import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HorseRaceTransactionMapper {

    HorseRaceTransactionInternalRequest toInternalRequest(UUID roomId,
                                                          RoomType roomType,
                                                          Integer winnerHorseIndex,
                                                          Collection<HorseRacePlayerBet> playerBets);
}
