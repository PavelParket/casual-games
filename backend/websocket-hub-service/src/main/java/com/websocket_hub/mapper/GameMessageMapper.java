package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.GameMessage;
import com.websocket_hub.enums.GameEvent;
import com.websocket_hub.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", imports = {MessageType.class})
public interface GameMessageMapper extends MessageMapper {

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM)")
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "nextPlayer", ignore = true)
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "message", ignore = true)
    GameMessage toGameStartMessageFromParams(GameEvent event, String roomId, Set<String> players);

    GameMessage toGameStartMessageFromEntity(GameMessage gameMessage);

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM)")
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "nextPlayer", ignore = true)
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "message", ignore = true)
    GameMessage toGameMoveMessage(GameEvent event, String roomId, String[] board, Integer cell, String player);
}
