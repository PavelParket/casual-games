package com.bank_service.factory;

import com.bank_service.domain.dto.HorseRaceTransactionRequest;
import com.bank_service.domain.entity.HorseRacePlayerBet;
import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class HorseRaceTransactionFactory implements GameTransactionFactory<HorseRaceTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.HORSE_RACE;
    }

    @Override
    public List<Transaction> createTransactions(HorseRaceTransactionRequest request) {
        return request.playerBets().stream()
                .map(bet -> buildTransaction(request, bet))
                .toList();
    }

    private Transaction buildTransaction(HorseRaceTransactionRequest request, HorseRacePlayerBet bet) {
        boolean isWinner = bet.getHorseIndex().equals(request.winnerHorseIndex());

        if (isWinner) {
            BigDecimal reward = bet.getAmount()
                    .multiply(BigDecimal.valueOf(bet.getOdd()))
                    .setScale(2, RoundingMode.HALF_UP);

            return buildEntry(request, bet, TransactionType.ADDITION, reward);
        } else {
            return buildEntry(request, bet, TransactionType.SUBTRACTION, bet.getAmount());
        }
    }

    private Transaction buildEntry(HorseRaceTransactionRequest request,
                                   HorseRacePlayerBet bet,
                                   TransactionType type,
                                   BigDecimal amount) {
        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? bet.getBalanceBefore().add(amount)
                : bet.getBalanceBefore().subtract(amount);

        return Transaction.builder()
                .userGuid(bet.getGuid())
                .roomId(request.roomId())
                .roomType(request.roomType())
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .balanceBefore(bet.getBalanceBefore())
                .balanceAfter(balanceAfter)
                .build();
    }
}
