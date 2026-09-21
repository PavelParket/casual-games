package com.casualgames.websockethub.mapper;

import com.casualgames.websockethub.domain.dto.response.PlayerResponse;
import com.casualgames.websockethub.domain.entity.ClientSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerResponse toResponse(ClientSession clientSession);
}
