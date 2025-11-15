package com.game_service.tic_tac_toe.mapper;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.GameEvent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "event", expression = "java(GameEvent.START.getDescription())")
    @Mapping(target = "board", source = "board")
    @Mapping(target = "nextPlayer", constant = "X")
    @Mapping(target = "playersSymbols", source = "playersSymbols")
    @Mapping(target = "players", source = "players")
    @Mapping(target = "message", expression = "java(\"Game started! X moves first.\")")
    GameResponse toStartResponse(GameRequest request, String[] board, Map<String, String> playersSymbols, Set<String> players);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "event", expression = "java(GameEvent.MOVE.getDescription())")
    @Mapping(target = "board", source = "board")
    @Mapping(target = "cell", source = "cell")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "nextPlayer", source = "nextPlayer")
    @Mapping(target = "message", expression = "java(\"Next move: \" + nextPlayer)")
    GameResponse toMoveResponse(GameRequest request, String[] board, Integer cell, String player, String nextPlayer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "event", expression = "java(event.getDescription())")
    @Mapping(target = "board", source = "board")
    @Mapping(target = "cell", source = "cell")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "nextPlayer", expression = "java(null)")
    @Mapping(target = "winner", source = "winner")
    @Mapping(target = "message", expression = "java(\"Player \" + winner + \" wins!\")")
    GameResponse toWinResponse(GameRequest request, GameEvent event, String[] board, Integer cell, String player, String winner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "event", expression = "java(GameEvent.DRAW.getDescription())")
    @Mapping(target = "board", source = "board")
    @Mapping(target = "cell", source = "cell")
    @Mapping(target = "player", source = "player")
    @Mapping(target = "nextPlayer", expression = "java(null)")
    @Mapping(target = "winner", constant = "draw")
    @Mapping(target = "message", constant = "It's a draw!")
    GameResponse toDrawResponse(GameRequest request, String[] board, Integer cell, String player);
}
