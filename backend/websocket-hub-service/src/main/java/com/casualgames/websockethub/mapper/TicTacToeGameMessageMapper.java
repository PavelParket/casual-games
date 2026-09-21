package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.message.TicTacToeGameMessage;
import com.casualgames.websockethub.domain.enums.MessageType;
import com.casualgames.websockethub.domain.enums.events.TicTacToeGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicTacToeGameMessageMapper extends MessageMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "currentPlayerSymbol", ignore = true)
    @Mapping(target = "nextPlayerSymbol", ignore = true)
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "bet", ignore = true)
    TicTacToeGameMessage toGameStartMessage(MessageType type, TicTacToeGameEvent event, UUID roomId, Map<UUID, String> players);

    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "nextPlayerSymbol", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "bet", ignore = true)
    TicTacToeGameMessage toGameMoveMessage(MessageType type,
                                           TicTacToeGameEvent event,
                                           UUID fromUserId,
                                           UUID roomId,
                                           String[] board,
                                           Integer cell,
                                           String currentPlayerSymbol,
                                           Map<UUID, String> playersSymbols,
                                           Map<UUID, String> players);
}
