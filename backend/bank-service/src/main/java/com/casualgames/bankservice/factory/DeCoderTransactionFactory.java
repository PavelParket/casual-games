package com.casualgames.bankservice.factory;

import com.casualgames.bankservice.domain.entity.Transaction;
import com.casualgames.bankservice.domain.enums.RoomType;
import com.casualgames.bankservice.domain.enums.TransactionStatus;
import com.casualgames.bankservice.domain.enums.TransactionType;
import com.casualgames.grpc.transaction.DeCoderPlayerTransaction;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DeCoderTransactionFactory implements GameTransactionFactory<DeCoderTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public List<Transaction> createTransactions(DeCoderTransactionRequest request) {
        List<Transaction> transactions = new ArrayList<>();

        request.getPlayerTransactionsList().forEach(transaction -> {
            BigDecimal before = new BigDecimal(transaction.getBalanceBefore());
            BigDecimal spent = new BigDecimal(transaction.getSpent());

            if (transaction.hasJackpot()) {
                BigDecimal jackpot = new BigDecimal(transaction.getJackpot());
                BigDecimal afterCredit = before.add(jackpot);
                BigDecimal afterDebit = afterCredit.subtract(spent);

                transactions.add(
                        buildTransaction(
                                request,
                                transaction,
                                TransactionType.ADDITION,
                                jackpot,
                                before,
                                afterCredit
                        )
                );

                transactions.add(
                        buildTransaction(
                                request,
                                transaction,
                                TransactionType.SUBTRACTION,
                                spent,
                                afterCredit,
                                afterDebit
                        )
                );
            } else {
                transactions.add(
                        buildTransaction(
                                request,
                                transaction,
                                TransactionType.SUBTRACTION,
                                spent,
                                before,
                                before.subtract(spent)
                        )
                );
            }
        });

        return transactions;
    }

    private Transaction buildTransaction(DeCoderTransactionRequest request,
                                         DeCoderPlayerTransaction transaction,
                                         TransactionType type,
                                         BigDecimal amount,
                                         BigDecimal balanceBefore,
                                         BigDecimal balanceAfter) {
        return Transaction.builder()
                .userGuid(UUID.fromString(transaction.getGuid()))
                .roomId(UUID.fromString(request.getRoomId()))
                .roomType(RoomType.valueOf(request.getRoomType()))
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }
}