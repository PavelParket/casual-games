package com.game_service.tic_tac_toe.mapper;

import com.game_service.common.enums.MessageType;
import com.game_service.tic_tac_toe.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
}
