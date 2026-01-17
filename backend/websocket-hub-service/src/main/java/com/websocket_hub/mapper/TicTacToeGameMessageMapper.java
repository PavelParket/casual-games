package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.TicTacToeGameMessage;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicTacToeGameMessageMapper extends MessageMapper {

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM)")
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
    TicTacToeGameMessage toGameStartMessageFromParams(TicTacToeGameEvent event, UUID roomId, Set<UUID> players);

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM)")
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "nextPlayerSymbol", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "bet", ignore = true)
    TicTacToeGameMessage toGameMoveMessage(
            TicTacToeGameEvent event,
            UUID fromUserId,
            UUID roomId,
            String[] board,
            Integer cell,
            String currentPlayerSymbol,
            Map<UUID, String> playersSymbols,
            Set<UUID> players
    );
}
