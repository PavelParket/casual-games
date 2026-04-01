package com.websocket_hub.validator;

import com.websocket_hub.domain.entity.PlayerBet;
import com.websocket_hub.domain.enums.ErrorCode;
import com.websocket_hub.exception.GameException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class PlayerBetValidator {

    public static final String BET_IS_NULL = "Bet is null!";
    public static final String USER_IS_MISSING = "User is missing!";
    public static final String BET_MUST_BE_POSITIVE = "Bet must be positive!";
    public static final String BALANCE_MUST_BE_GREATER_THAN_ZERO = "Balance must be greater than 0 (zero)!";
    public static final String BET_MUST_NOT_BE_LESS_THAN_BALANCE = "Bet must not be less than balance!";
    public static final String BOTH_PLAYERS_MUST_PLACE_BETS = "Both players must place bets before game starts";
    public static final String BETS_MUST_BE_EQUAL = "Bets must be equal to start the game";

    public void validateBet(PlayerBet playerBet) {
        if (playerBet == null) {
            throw new GameException(ErrorCode.VALIDATION_ERROR, BET_IS_NULL);
        }

        if (playerBet.getGuid() == null) {
            throw new GameException(ErrorCode.VALIDATION_ERROR, USER_IS_MISSING);
        }

        if (playerBet.getBet() == null || playerBet.getBet().compareTo(BigDecimal.ZERO) <= 0) {
            throw new GameException(ErrorCode.VALIDATION_ERROR, BET_MUST_BE_POSITIVE);
        }

        if (playerBet.getBalanceBefore() == null || playerBet.getBalanceBefore().compareTo(BigDecimal.ZERO) < 0) {
            throw new GameException(ErrorCode.VALIDATION_ERROR, BALANCE_MUST_BE_GREATER_THAN_ZERO);
        }

        if (playerBet.getBet().compareTo(playerBet.getBalanceBefore()) > 0) {
            throw new GameException(ErrorCode.INSUFFICIENT_BALANCE, BET_MUST_NOT_BE_LESS_THAN_BALANCE);
        }
    }

    public void validateBetsForGameStart(List<PlayerBet> bets) {
        if (bets == null || bets.size() != 2) {
            log.warn("Cannot start game - expected 2 bets, got: {}", bets == null ? 0 : bets.size());
            throw new GameException(ErrorCode.VALIDATION_ERROR, BOTH_PLAYERS_MUST_PLACE_BETS);
        }

        PlayerBet bet1 = bets.get(0);
        PlayerBet bet2 = bets.get(1);

        if (bet1.getBet().compareTo(bet2.getBet()) != 0) {
            log.warn("Cannot start game - bets are not equal: {} vs {}", bet1.getBet(), bet2.getBet());
            throw new GameException(ErrorCode.VALIDATION_ERROR, BETS_MUST_BE_EQUAL);
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
