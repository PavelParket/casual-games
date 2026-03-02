package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameTransactionMapper {

    DeCoderTransactionInternalRequest toInternalRequest(UUID roomId, RoomType roomType, PlayerBet playerBet, UUID winner);
}
