package com.websocket_hub.validator;

import com.websocket_hub.domain.entity.HorseRacePlayerBet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class HorseRacePlayerBetValidator {

    public void validateBet(HorseRacePlayerBet bet, Integer horseCount) {
        if (bet == null) {
            throw new IllegalArgumentException("Bet is null!");
        }

        if (bet.getGuid() == null) {
            throw new IllegalArgumentException("User is missing!");
        }

        if (bet.getAmount() == null || bet.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive!");
        }

        if (bet.getBalanceBefore() == null || bet.getBalanceBefore().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance must be non-negative!");
        }

        if (bet.getAmount().compareTo(bet.getBalanceBefore()) > 0) {
            throw new IllegalArgumentException("Bet amount exceeds balance!");
        }

        if (bet.getHorseIndex() == null || bet.getHorseIndex() < 0 || bet.getHorseIndex() >= horseCount) {
            throw new IllegalArgumentException(
                    String.format("Invalid horse index: %d. Must be between 0 and %d", bet.getHorseIndex(), horseCount - 1)
            );
        }
    }

    public boolean hasPlayerPlacedBet(UUID playerGuid, HorseRacePlayerBet bet) {
        return bet != null && playerGuid.equals(bet.getGuid());
    }
}
