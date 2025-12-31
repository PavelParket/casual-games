package com.bank_service.domain.dto;

import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record TransactionResponse(
        Long id,

        UUID roomId,

        RoomType roomType,

        TransactionType type,

        TransactionStatus status,

        BigDecimal amount,

        BigDecimal balanceBefore,

        BigDecimal balanceAfter,

        LocalDate createdAtDate,

        LocalTime createdAtTime
) {
}
