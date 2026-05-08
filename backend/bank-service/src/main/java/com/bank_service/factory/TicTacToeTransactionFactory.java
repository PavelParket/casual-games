package com.bank_service.factory;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.common_utils.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_LOSER;
import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_WINNER;

@Component
public class TicTacToeTransactionFactory implements GameTransactionFactory<TicTacToeTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.TIC_TAC_TOE;
    }

    @Override
    public List<Transaction> createTransactions(TicTacToeTransactionRequest request) {
        UUID winnerGuid = UUID.fromString(request.getWinner());
        UUID roomId = UUID.fromString(request.getRoomId());
        RoomType roomType = RoomType.valueOf(request.getRoomType());

        PlayerBetMessage winner = request.getPlayerBetsList().stream()
                .filter(bet -> UUID.fromString(bet.getGuid()).equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_WINNER));

        PlayerBetMessage loser = request.getPlayerBetsList().stream()
                .filter(bet -> !UUID.fromString(bet.getGuid()).equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_LOSER));

        BigDecimal reward = new BigDecimal(loser.getBet());

        return List.of(
                buildTransaction(roomId, roomType, winner, TransactionType.ADDITION, reward),
                buildTransaction(roomId, roomType, loser, TransactionType.SUBTRACTION, reward)
        );
    }

    private Transaction buildTransaction(UUID roomId,
                                         RoomType roomType,
                                         PlayerBetMessage bet,
                                         TransactionType type,
                                         BigDecimal amount) {
        BigDecimal balanceBefore = new BigDecimal(bet.getBalanceBefore());
        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? balanceBefore.add(amount)
                : balanceBefore.subtract(amount);

        return Transaction.builder()
                .userGuid(UUID.fromString(bet.getGuid()))
                .roomId(roomId)
                .roomType(roomType)
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }
}
