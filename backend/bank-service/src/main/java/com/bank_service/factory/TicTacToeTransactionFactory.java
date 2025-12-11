package com.bank_service.factory;

import com.bank_service.domain.dto.GameTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.bank_service.exception.PlayerNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TicTacToeTransactionFactory implements GameTransactionFactory {

    @Override
    public RoomType getRoomType() {
        return RoomType.TIC_TAC_TOE;
    }

    @Override
    public List<Transaction> createTransactions(GameTransactionRequest gameTransactionRequest) {
        UUID winnerGuid = gameTransactionRequest.winners().getFirst();

        PlayerBet winner = gameTransactionRequest.playerBets().stream()
                .filter(playerBet -> playerBet.getGuid().equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Winner not found!"));

        PlayerBet loser = gameTransactionRequest.playerBets().stream()
                .filter(playerBet -> !playerBet.getGuid().equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Loser not found!"));

        BigDecimal reward = loser.getBet();

        return List.of(
                buildTransaction(gameTransactionRequest, winner, TransactionType.ADDITION, reward),
                buildTransaction(gameTransactionRequest, loser, TransactionType.SUBTRACTION, reward)
        );
    }

    private Transaction buildTransaction(GameTransactionRequest gameTransactionRequest,
                                         PlayerBet playerBet,
                                         TransactionType type,
                                         BigDecimal amount) {

        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? playerBet.getBalanceBefore().add(amount)
                : playerBet.getBalanceBefore().subtract(amount);

        return Transaction.builder()
                .userGuid(playerBet.getGuid())
                .roomId(gameTransactionRequest.roomId())
                .roomType(gameTransactionRequest.roomType())
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(playerBet.getBalanceBefore())
                .balanceAfter(balanceAfter)
                .build();
    }
}
