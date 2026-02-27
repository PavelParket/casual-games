package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.bank_service.DeCoderTransactionInternalRequest;
import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DeCoderGameTransactionMapper {

    DeCoderTransactionInternalRequest toInternalRequest(UUID roomId, RoomType roomType, PlayerBet playerBet, UUID winner);
}
