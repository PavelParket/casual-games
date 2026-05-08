package com.bank_service.factory;


import com.bank_service.domain.entity.Transaction;
import com.bank_service.domain.enums.RoomType;
import com.bank_service.domain.enums.TransactionStatus;
import com.bank_service.domain.enums.TransactionType;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        PlayerBetMessage bet = request.getPlayerBet();
        UUID roomId = UUID.fromString(request.getRoomId());
        RoomType roomType = RoomType.valueOf(request.getRoomType());
        BigDecimal betAmount = new BigDecimal(bet.getBet());
        BigDecimal balanceBefore = new BigDecimal(bet.getBalanceBefore());

        TransactionType type = request.hasWinner() ? TransactionType.ADDITION : TransactionType.SUBTRACTION;
        BigDecimal balanceAfter = TransactionType.ADDITION.equals(type)
                ? balanceBefore.add(betAmount)
                : balanceBefore.subtract(betAmount);

        return List.of(Transaction.builder()
                .userGuid(UUID.fromString(bet.getGuid()))
                .roomId(roomId)
                .roomType(roomType)
                .type(type)
                .status(TransactionStatus.PENDING)
                .amount(betAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .build());
    }
}