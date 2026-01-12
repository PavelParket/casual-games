package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.RoomResponse;
import com.websocket_hub.domain.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "participantGuids", expression = "java(room.getParticipantGuids())")
    @Mapping(target = "participantCount", expression = "java(room.size())")
    RoomResponse toResponse(Room room);
}
