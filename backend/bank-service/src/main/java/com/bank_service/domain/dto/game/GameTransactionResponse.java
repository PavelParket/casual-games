package com.bank_service.domain.dto.game;

import com.bank_service.domain.enums.RoomType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record GameTransactionResponse(

        UUID roomId,

        RoomType roomType,

        int transactionCount,

        Instant processedAt
) {
}
