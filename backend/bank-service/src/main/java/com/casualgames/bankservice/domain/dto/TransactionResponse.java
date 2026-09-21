package com.casualgames.bankservice.domain.dto;

import com.casualgames.bankservice.domain.enums.RoomType;
import com.casualgames.bankservice.domain.enums.TransactionStatus;
import com.casualgames.bankservice.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private UUID roomId;

    private RoomType roomType;

    private TransactionType type;

    private TransactionStatus status;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private LocalDate createdAtDate;

    private LocalTime createdAtTime;
}
