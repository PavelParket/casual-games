package com.websocket_hub.mapper;

import com.websocket_hub.dto.RoomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "type", constant = "system")
    @Mapping(target = "fromUserId", constant = "system")
    @Mapping(target = "toUserId", constant = "")
    RoomResponse toResponse(String roomId, String content);
}
