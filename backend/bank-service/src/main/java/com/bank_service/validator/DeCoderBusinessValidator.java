package com.bank_service.validator;

import com.bank_service.domain.enums.RoomType;
import com.casualgames.grpc.transaction.DeCoderTransactionRequest;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import static com.bank_service.config.ResourceMessageConstants.INSUFFICIENT_FUNDS;
import static com.bank_service.config.ResourceMessageConstants.INVALID_BET_AMOUNT;
import static com.bank_service.config.ResourceMessageConstants.WINNER_GUID_MISMATCH;

@Component
@Slf4j
public class DeCoderBusinessValidator implements GameBusinessValidator<DeCoderTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.DE_CODER;
    }

    @Override
    public void validate(DeCoderTransactionRequest request) {
        PlayerBetMessage bet = request.getPlayerBet();
        BigDecimal betAmount = new BigDecimal(bet.getBet());
        BigDecimal balanceBefore = new BigDecimal(bet.getBalanceBefore());
        UUID playerGuid = UUID.fromString(bet.getGuid());

        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(INVALID_BET_AMOUNT);
        }

        if (!request.hasWinner()) {
            if (balanceBefore.compareTo(betAmount) < 0) {
                log.warn("Insufficient balance for De-Coder move: user={}, balance={}, cost={}", playerGuid, balanceBefore, betAmount);

                throw new BadRequestException(String.format(INSUFFICIENT_FUNDS, balanceBefore, betAmount));
            }
        }

        if (request.hasWinner() && !UUID.fromString(request.getWinner()).equals(playerGuid)) {
            throw new BadRequestException(WINNER_GUID_MISMATCH);
        }
    }
}