package com.casualgames.bankservice.factory;

import com.casualgames.bankservice.domain.dto.DepositRequest;
import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.bankservice.domain.enums.TransactionStatus;
import com.casualgames.bankservice.domain.enums.TransactionType;
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