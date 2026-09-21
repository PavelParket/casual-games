package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.response.RoomResponse;
import com.casualgames.websockethub.domain.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "participantGuids", expression = "java(room.getParticipantGuids())")
    @Mapping(target = "participantCount", expression = "java(room.size())")
    RoomResponse toResponse(Room room);
}
