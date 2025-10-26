package com.game_service.tic_tac_toe.mapper;

import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "type", expression = "java(MessageType.START)")
    @Mapping(target = "nextPlayer", constant = "X")
    @Mapping(target = "message", expression = "java(\"Game started! X moves first.\")")
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "winner", ignore = true)
    GameResponse toStartResponse(String roomName, String[][] board, Map<String, String> playersSymbols, Set<String> players);

    @Mapping(target = "type", expression = "java(MessageType.MOVE)")
    @Mapping(target = "message", expression = "java(\"Next move: \" + nextPlayer)")
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "winner", ignore = true)
    GameResponse toMoveResponse(String roomName, String[][] board, Integer cell, String player, String nextPlayer);

    @Mapping(target = "type", expression = "java(type)")
    @Mapping(target = "nextPlayer", expression = "java(null)")
    @Mapping(target = "message", expression = "java(\"Player \" + winner + \" wins!\")")
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "players", ignore = true)
    GameResponse toWinResponse(MessageType type, String roomName, String[][] board, Integer cell, String player, String winner);

    @Mapping(target = "type", expression = "java(MessageType.DRAW)")
    @Mapping(target = "nextPlayer", expression = "java(null)")
    @Mapping(target = "message", constant = "It's a draw!")
    @Mapping(target = "playersSymbols", ignore = true)
    @Mapping(target = "players", ignore = true)
    @Mapping(target = "winner", ignore = true)
    GameResponse toDrawResponse(String roomName, String[][] board, Integer cell, String player);
}
