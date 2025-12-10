package com.bank_service.domain.dto;

import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record GameTransactionRequest(

        UUID roomId,

        RoomType roomType,

        TransactionType type,

        TransactionStatus status,

        List<PlayerBet> playerBets,

        List<UUID> winners,

        Map<String, Object> metadata
) {
}
