package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.RoomMessage;
import com.websocket_hub.domain.enums.EventType;
import com.websocket_hub.domain.enums.MessageType;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MessageMapper {

    @Mapping(target = "event", expression = "java(event.getDescription())")
    @Mapping(target = "fromUserId", constant = "system")
    @Mapping(target = "toUserId", ignore = true)
    RoomMessage toResponse(MessageType type, EventType event, String roomName, String message);
}

