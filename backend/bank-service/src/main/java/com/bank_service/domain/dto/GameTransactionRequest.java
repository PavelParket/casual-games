package com.bank_service.domain.dto;

import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.enums.RoomType;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record GameTransactionRequest(

        UUID roomId,

        RoomType roomType,

        List<PlayerBet> playerBets,

        List<UUID> winners,

        Map<String, Object> metadata
) {
}
