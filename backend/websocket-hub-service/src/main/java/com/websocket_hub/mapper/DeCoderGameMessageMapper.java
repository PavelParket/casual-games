package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.DeCoderGameInternalRequest;
import com.websocket_hub.domain.dto.client.DeCoderGameInternalResponse;
import com.websocket_hub.domain.dto.message.DeCoderGameMessage;
import com.websocket_hub.domain.enums.events.DeCoderGameEvent;
import com.websocket_hub.domain.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameMessageMapper extends MessageMapper {

    @Mapping(target = "code", ignore = true)
    DeCoderGameInternalRequest toStartRequest(DeCoderGameEvent event,
                                              UUID roomId);

    DeCoderGameInternalRequest toMoveRequest(DeCoderGameEvent event,
                                             UUID roomId,
                                             UUID player,
                                             String code);

    @Mapping(target = "code", ignore = true)
    DeCoderGameMessage toMessage(DeCoderGameInternalResponse deCoderGameInternalResponse,
                                 MessageType type,
                                 UUID fromUserId,
                                 UUID toUserId);
}