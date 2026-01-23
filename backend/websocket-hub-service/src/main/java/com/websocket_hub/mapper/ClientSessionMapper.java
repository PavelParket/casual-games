package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.game_service.PlayerInternalRequest;
import com.websocket_hub.domain.entity.ClientSession;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientSessionMapper {

    PlayerInternalRequest toPlayerInternalRequest(ClientSession clientSession);

    List<PlayerInternalRequest> toPlayerInternalRequestList(List<ClientSession> clientSessions);
}
