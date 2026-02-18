package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.message.DefaultMessage;
import com.websocket_hub.domain.enums.MessageType;
import com.websocket_hub.domain.enums.events.EventType;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    DefaultMessage toResponse(MessageType type,
                              EventType event,
                              UUID fromUserId,
                              UUID toUserId,
                              UUID roomId,
                              String message);
}
