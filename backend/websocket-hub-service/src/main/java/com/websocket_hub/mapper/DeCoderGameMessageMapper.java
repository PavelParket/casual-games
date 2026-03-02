package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.domain.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameMessageMapper extends MessageMapper {

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "winner", ignore = true)
    DeCoderGameMessage toGameStartMessage(MessageType type, DeCoderGameEvent event, UUID roomId, UUID player);

    @Mapping(target = "fromUserId", ignore = true)
    @Mapping(target = "toUserId", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "winner", ignore = true)
    DeCoderGameMessage toGameMoveMessage(MessageType type, DeCoderGameEvent event, UUID roomId, UUID player, Integer code);
}