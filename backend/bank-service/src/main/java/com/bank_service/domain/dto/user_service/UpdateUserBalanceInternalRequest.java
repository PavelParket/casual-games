package com.bank_service.domain.dto.user_service;

import com.bank_service.domain.entity.TransactionShortInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateUserBalanceInternalRequest(
        List<TransactionShortInfo> transactionShortInfoList
) {
}
