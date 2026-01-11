package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.TicTacToeGameMessage;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.TicTacToeGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", imports = {MessageType.class})
public interface TicTacToeGameMessageMapper extends MessageMapper {

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM.getType())")
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "nextPlayer", ignore = true)
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "message", ignore = true)
    TicTacToeGameMessage toGameStartMessageFromParams(TicTacToeGameEvent event, String roomName, Set<String> players);

    @Mapping(target = "type", expression = "java(MessageType.SYSTEM.getType())")
    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "nextPlayer", ignore = true)
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "bet", ignore = true)
    @Mapping(target = "message", ignore = true)
    TicTacToeGameMessage toGameMoveMessage(TicTacToeGameEvent event, String roomName, String[] board, Integer cell, String player);
}
