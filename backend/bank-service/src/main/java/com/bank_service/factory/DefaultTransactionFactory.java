package com.bank_service.factory;

import com.bank_service.domain.dto.DepositRequest;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultTransactionFactory {

    public Transaction createTransaction(DepositRequest request, BigDecimal balanceBefore) {
        BigDecimal balanceAfter = balanceBefore.add(request.amount());

        return Transaction.builder()
                .userGuid(request.userGuid())
                .roomId(null)
                .roomType(null)
                .type(TransactionType.ADDITION)
                .status(TransactionStatus.PENDING)
                .amount(request.amount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }
}