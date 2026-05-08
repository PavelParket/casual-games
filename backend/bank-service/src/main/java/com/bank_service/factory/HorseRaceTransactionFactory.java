package com.bank_service.factory;

import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.casualgames.grpc.transaction.HorseRacePlayerBetMessage;
import com.casualgames.grpc.transaction.HorseRaceTransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Component
public class HorseRaceTransactionFactory implements GameTransactionFactory<HorseRaceTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.HORSE_RACE;
    }

    @Override
    public List<Transaction> createTransactions(HorseRaceTransactionRequest request) {
        UUID roomId = UUID.fromString(request.getRoomId());
        RoomType roomType = RoomType.valueOf(request.getRoomType());

        return request.getPlayerBetsList().stream()
                .map(bet -> buildTransaction(request.getWinnerHorseIndex(), roomId, roomType, bet))
                .toList();
    }

    private Transaction buildTransaction(int winnerHorseIndex,
                                         UUID roomId,
                                         RoomType roomType,
                                         HorseRacePlayerBetMessage bet) {
        boolean isWinner = bet.getHorseIndex() == winnerHorseIndex;
        BigDecimal amount = new BigDecimal(bet.getAmount());
        BigDecimal balanceBefore = new BigDecimal(bet.getBalanceBefore());

        TransactionType type;
        BigDecimal transactionAmount;

        if (isWinner) {
            type = TransactionType.ADDITION;
            transactionAmount = amount.multiply(BigDecimal.valueOf(bet.getOdd()))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            type = TransactionType.SUBTRACTION;
            transactionAmount = amount;
        }

        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? balanceBefore.add(transactionAmount)
                : balanceBefore.subtract(transactionAmount);

        return Transaction.builder()
                .userGuid(UUID.fromString(bet.getGuid()))
                .roomId(roomId)
                .roomType(roomType)
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(transactionAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build();
    }
}
