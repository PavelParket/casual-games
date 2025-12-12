package com.bank_service.domain.entity;

import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class TransactionShortInfo {

    private final Long id;

    private final UUID userGuid;

    private final BigDecimal amount;

    private final TransactionType type;

    private TransactionStatus status;
}
