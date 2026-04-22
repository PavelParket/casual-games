package com.bank_service.validator;

import com.bank_service.domain.dto.game.DeCoderTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.bank_service.config.ResourceMessageConstants.INSUFFICIENT_FUNDS;
import static com.bank_service.config.ResourceMessageConstants.INVALID_BET_AMOUNT;
import static com.bank_service.config.ResourceMessageConstants.WINNER_GUID_MISMATCH;

@Component
@Slf4j
public class DeCoderBusinessValidator implements GameBusinessValidator<DeCoderTransactionRequest> {

    @Override
    public void validate(DeCoderTransactionRequest request) {
        PlayerBet bet = request.playerBet();

        if (bet.getBet().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(INVALID_BET_AMOUNT);
        }

        if (!request.isWin()) {
            if (bet.getBalanceBefore().compareTo(bet.getBet()) < 0) {
                log.warn("Insufficient balance for De-Coder move: user={}, balance={}, cost={}", bet.getGuid(), bet.getBalanceBefore(), bet.getBet());

                throw new BadRequestException(String.format(INSUFFICIENT_FUNDS, bet.getBalanceBefore(), bet.getBet()));
            }
        }

        if (request.isWin() && !request.winner().equals(bet.getGuid())) {
            throw new BadRequestException(WINNER_GUID_MISMATCH);
        }
    }
}