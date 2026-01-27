package com.websocket_hub.validator;

import com.websocket_hub.domain.dto.bank_service.PlayerBet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class PlayerBetValidator {

    public void validateBet(PlayerBet playerBet) {
        if (playerBet == null) {
            throw new IllegalArgumentException("Bet is null!");
        }

        if (playerBet.getGuid() == null) {
            throw new IllegalArgumentException("User is missing!");
        }

        if (playerBet.getBet() == null || playerBet.getBet().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bet must be positive!");
        }

        if (playerBet.getBalanceBefore() == null || playerBet.getBalanceBefore().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance must be greater than 0 (zero)!");
        }

        if (playerBet.getBet().compareTo(playerBet.getBalanceBefore()) > 0) {
            throw new IllegalArgumentException("Bet must not be less than balance!");
        }
    }

    public void validateBetsCount(List<PlayerBet> playerBets) {
        if (playerBets.size() > 2) {
            throw new IllegalStateException("Too many bets in room!");
        }
    }
}
