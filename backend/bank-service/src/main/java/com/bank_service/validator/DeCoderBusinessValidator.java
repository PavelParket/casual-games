package com.bank_service.validator;

import com.bank_service.domain.dto.DeCoderTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.exception.BusinessValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class DeCoderBusinessValidator implements GameBusinessValidator<DeCoderTransactionRequest> {

    @Override
    public void validate(DeCoderTransactionRequest request) {
        PlayerBet bet = request.playerBet();

        if (bet.getBet().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Amount must be greater than 0");
        }

        if (!request.isWin()) {
            if (bet.getBalanceBefore().compareTo(bet.getBet()) < 0) {
                log.warn("Insufficient balance for De-Coder move: user={}, balance={}, cost={}",
                        bet.getGuid(), bet.getBalanceBefore(), bet.getBet());

                throw new BusinessValidationException(
                        String.format("Insufficient funds. Balance: %s, Required: %s",
                                bet.getBalanceBefore(), bet.getBet())
                );
            }
        }

        if (request.isWin() && !request.winner().equals(bet.getGuid())) {
            throw new BusinessValidationException("Winner GUID does not match player GUID in payload");
        }
    }
}