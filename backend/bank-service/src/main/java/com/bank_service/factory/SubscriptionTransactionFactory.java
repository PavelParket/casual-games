package com.bank_service.factory;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.casualgames.grpc.subscription.SubscriptionTransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SubscriptionTransactionFactory {

    public Transaction createSubscriptionTransaction(SubscriptionTransactionRequest request, BigDecimal balanceBefore) {
        BigDecimal amount = new BigDecimal(request.getAmount());

        return Transaction.builder()
                .userGuid(UUID.fromString(request.getUserGuid()))
                .type(TransactionType.valueOf(request.getType()))
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore.subtract(amount))
                .build();
    }

    public Transaction createSubtractionTransaction(Transaction transaction) {
        return Transaction.builder()
                .userGuid(transaction.getUserGuid())
                .type(TransactionType.SUBTRACTION)
                .status(TransactionStatus.PENDING)
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .build();
    }
}
