package com.game_service.de_coder.mapper;

import com.game_service.de_coder.dto.GameResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", implementationName = "DeCodeGameMapperImpl")
public interface GameMapper {

    @Mapping(target = "type", expression = "java(MessageType.START)")
    @Mapping(target = "message", expression = "java(\"Game started!\")")
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "winner", ignore = true)
    GameResponse toStartResponse(String roomName, Set<String> players);

    @Mapping(target = "type", expression = "java(MessageType.MOVE)")
    @Mapping(target = "message", expression = "java(\"Does not match the winning code\")")
    @Mapping(target = "winner", ignore = true)
    GameResponse toMoveResponse(String roomName, String player, Integer code, Set<String> players);

    @Mapping(target = "type", expression = "java(MessageType.WINNER)")
    @Mapping(target = "message", expression = "java(\"Player \" + winner + \" wins!\")")
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "player", ignore = true)
    GameResponse toWinResponse(String roomName, String winner);
}
