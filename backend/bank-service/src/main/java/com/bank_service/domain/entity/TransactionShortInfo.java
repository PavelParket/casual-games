package com.bank_service.domain.entity;

import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionShortInfo {

    private Long id;

    private UUID userGuid;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;
}
