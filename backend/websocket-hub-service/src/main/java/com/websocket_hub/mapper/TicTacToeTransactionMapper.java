package com.websocket_hub.mapper;

import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import com.websocket_hub.domain.dto.bank_service.TicTacToeTransactionInternalRequest;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicTacToeTransactionMapper {

    TicTacToeTransactionInternalRequest toInternalRequest(UUID roomId, List<PlayerBet> playerBets, UUID winner);
}
