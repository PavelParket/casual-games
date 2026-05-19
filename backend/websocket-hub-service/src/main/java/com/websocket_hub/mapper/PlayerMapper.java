package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.response.PlayerResponse;
import com.websocket_hub.domain.entity.ClientSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerResponse toResponse(ClientSession clientSession);
}
