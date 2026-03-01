package com.websocket_hub.validator;

import com.websocket_hub.domain.entity.PlayerBet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    public void validateBetsForGameStart(List<PlayerBet> bets) {
        if (bets == null || bets.size() != 2) {
            log.warn("Cannot start game - expected 2 bets, got: {}", bets == null ? 0 : bets.size());
            throw new IllegalStateException("Both players must place bets before game starts");
        }

        PlayerBet bet1 = bets.get(0);
        PlayerBet bet2 = bets.get(1);

        if (bet1.getBet().compareTo(bet2.getBet()) != 0) {
            log.warn("Cannot start game - bets are not equal: {} vs {}", bet1.getBet(), bet2.getBet());
            throw new IllegalStateException("Bets must be equal to start the game");
        }

        log.info("Bets validated for game start: {} from 2 players", bet1.getBet());
    }

    public boolean hasPlayerPlacedBet(List<PlayerBet> bets, UUID playerGuid) {
        if (bets == null || bets.isEmpty()) {
            return false;
        }

        return bets.stream()
                .anyMatch(bet -> bet.getGuid().equals(playerGuid));
    }
}
