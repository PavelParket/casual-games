package com.bank_service.factory;

import com.bank_service.domain.dto.game.DurakTransactionRequest;
import com.bank_service.domain.dto.game.GameTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.common_utils.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_LOSER;
import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_WINNER;

@Component
public class DurakTransactionFactory implements GameTransactionFactory<DurakTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.DURAK;
    }

    @Override
    public List<Transaction> createTransactions(DurakTransactionRequest request) {
        UUID winnerGuid = request.winner();

        PlayerBet winner = request.playerBets().stream()
                .filter(playerBet -> playerBet.getGuid().equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_WINNER));

        PlayerBet loser = request.playerBets().stream()
                .filter(bet -> !bet.getGuid().equals(winnerGuid))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_LOSER));

        BigDecimal reward = loser.getBet();

        return List.of(
                buildTransaction(request, winner, TransactionType.ADDITION, reward),
                buildTransaction(request, loser, TransactionType.SUBTRACTION, reward)
        );
    }

    private Transaction buildTransaction(GameTransactionRequest request,
                                         PlayerBet playerBet,
                                         TransactionType type,
                                         BigDecimal amount) {
        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? playerBet.getBalanceBefore().add(amount)
                : playerBet.getBalanceBefore().subtract(amount);

        return Transaction.builder()
                .userGuid(playerBet.getGuid())
                .roomId(request.roomId())
                .roomType(request.roomType())
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(playerBet.getBalanceBefore())
                .balanceAfter(balanceAfter)
                .build();
    }
}
