package com.game_service.tic_tac_toe.mapper;

import com.game_service.common.dto.TicTacToeGameMatchResponse;
import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import com.game_service.common.enums.MessageType;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.domain.entity.TicTacToe;
import com.game_service.tic_tac_toe.domain.enums.TicTacToeGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicTacToeGameMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "winner", ignore = true)
    TicTacToeGameResponse toStartResponse(MessageType type,
                                          TicTacToeGameEvent event,
                                          UUID roomId,
                                          String[] board,
                                          String currentPlayerSymbol,
                                          String nextPlayerSymbol,
                                          Map<UUID, String> playersSymbols,
                                          Map<UUID, String> players,
                                          String message);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    TicTacToeGameResponse toMoveResponse(MessageType type,
                                         TicTacToeGameEvent event,
                                         UUID roomId,
                                         String message,
                                         String[] board,
                                         Integer cell,
                                         String currentPlayerSymbol,
                                         String nextPlayerSymbol,
                                         Map<UUID, String> playersSymbols,
                                         Map<UUID, String> players,
                                         UUID winner);

    @Mapping(target = "gameType", source = "gameType")
    @Mapping(target = "gameResult", source = "gameResult")
    @Mapping(target = "players", source = "ticTacToe.players", qualifiedByName = "mapPlayersToList")
    TicTacToeGameMatchResponse toMatchResponse(TicTacToe ticTacToe,
                                               UUID userGuid,
                                               GameType gameType,
                                               GameResult gameResult);

    @Named("mapPlayersToList")
    default List<UUID> mapPlayersToList(Map<UUID, String> players) {
        return players == null ? List.of() : List.copyOf(players.keySet());
    }
}
