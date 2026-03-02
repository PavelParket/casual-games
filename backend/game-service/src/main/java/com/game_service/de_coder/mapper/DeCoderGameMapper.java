package com.game_service.de_coder.mapper;

import com.game_service.common.enums.MessageType;
import com.game_service.de_coder.dto.DeCoderGameResponse;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "gameState", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    DeCoderGameResponse toStartResponse(MessageType type,
                                        DeCoderGameEvent event,
                                        UUID roomId, String message, UUID player);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "gameState", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    DeCoderGameResponse toMoveResponse(MessageType type,
                                       DeCoderGameEvent event,
                                       UUID roomId, String message, Integer code, UUID player);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "gameState", ignore = true)
    @Mapping(target = "isGameStarted", ignore = true)
    DeCoderGameResponse toWinResponse(MessageType type,
                                      DeCoderGameEvent event,
                                      UUID roomId, String message, UUID winner);
}