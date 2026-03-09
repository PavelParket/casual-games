package com.websocket_hub.mapper;

import com.websocket_hub.exception.GameException;
import org.mapstruct.Mapper;
import org.springframework.web.client.HttpClientErrorException;

@Mapper(componentModel = "spring")
public interface ErrorMapper {

    default GameException mapToGameException(HttpClientErrorException e) {
        String
    }
}
