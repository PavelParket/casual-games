package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.client.TicTacToeTransactionInternalRequest;
import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.RoomType;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicTacToeTransactionMapper {

    TicTacToeTransactionInternalRequest toInternalRequest(UUID roomId,
                                                          RoomType roomType,
                                                          List<PlayerBet> playerBets,
                                                          UUID winner);
}
