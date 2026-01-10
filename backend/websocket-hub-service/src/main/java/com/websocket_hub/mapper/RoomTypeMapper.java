package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.RoomTypeResponse;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomTypeMapper {

    @Mapping(target = "name", expression = "java(roomType.name())")
    RoomTypeResponse toResponse(RoomType roomType);
}
