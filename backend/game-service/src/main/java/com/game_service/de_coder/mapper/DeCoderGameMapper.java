package com.game_service.de_coder.mapper;

import com.game_service.de_coder.domain.dto.DeCoderGameResponse;
import com.game_service.de_coder.domain.entity.DeCoderGameState;
import com.game_service.de_coder.domain.enums.DeCoderGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameMapper {

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "gameState", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    @Mapping(target = "jackpot", ignore = true)
    DeCoderGameResponse toStartResponse(DeCoderGameEvent event,
                                        UUID roomId,
                                        String message);

    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    @Mapping(target = "jackpot", ignore = true)
    DeCoderGameResponse toMoveResponse(DeCoderGameEvent event,
                                       UUID roomId,
                                       String message,
                                       List<DeCoderGameState> gameState,
                                       UUID player);

    @Mapping(target = "player", ignore = true)
    @Mapping(target = "gameState", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    DeCoderGameResponse toWinResponse(DeCoderGameEvent event,
                                      UUID roomId,
                                      String message,
                                      BigDecimal jackpot,
                                      UUID winner);
}