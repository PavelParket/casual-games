package com.bank_service.factory;

import com.bank_service.domain.dto.DeCoderTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DeCoderTransactionFactory implements GameTransactionFactory<DeCoderTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public List<Transaction> createTransactions(DeCoderTransactionRequest request) {
        PlayerBet playerBet = request.playerBet();
        TransactionType type;
        BigDecimal balanceAfter;

        if (request.isWin()) {
            type = TransactionType.ADDITION;
            balanceAfter = playerBet.getBalanceBefore().add(playerBet.getBet());
        } else {
            type = TransactionType.SUBTRACTION;
            balanceAfter = playerBet.getBalanceBefore().subtract(playerBet.getBet());
        }
        System.out.println("\nplayerBet" + playerBet + "\ntype" + type + "\nbalanceAfter: " + balanceAfter + "winner: " + request.winner());
        Transaction transaction = Transaction.builder()
                .userGuid(playerBet.getGuid())
                .roomId(request.roomId())
                .roomType(RoomType.DE_CODER)
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(playerBet.getBet())
                .balanceBefore(playerBet.getBalanceBefore())
                .balanceAfter(balanceAfter)
                .build();

        return List.of(transaction);
    }
}