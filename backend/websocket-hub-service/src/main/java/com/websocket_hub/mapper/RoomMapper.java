package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.RoomInfoResponse;
import com.websocket_hub.domain.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "participantEmails", expression = "java(room.getParticipantEmails())")
    @Mapping(target = "participantCount", expression = "java(room.size())")
    RoomInfoResponse toResponse(Room room);
}
