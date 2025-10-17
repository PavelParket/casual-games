package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.enums.MessageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "fromUserId", constant = "system")
    @Mapping(target = "toUserId", constant = "")
    RoomResponse toResponse(MessageType type, String roomId, String content);
}
