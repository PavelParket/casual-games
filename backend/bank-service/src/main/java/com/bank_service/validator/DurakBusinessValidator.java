package com.bank_service.validator;

import com.bank_service.domain.dto.DurakTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.bank_service.exception.BusinessValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class DurakBusinessValidator implements GameBusinessValidator<DurakTransactionRequest> {

    @Override
    public void validate(DurakTransactionRequest request) {
        List<PlayerBet> playerBets = request.playerBets();

        validateEqualBets(playerBets);
        validateSufficientBalance(playerBets);

        if (!request.isDraw()) {
            validateWinnerExists(request.winner(), playerBets);
        }

        validateNoOverflow(playerBets);
    }

    private void validateEqualBets(List<PlayerBet> bets) {
        if (bets.size() != 2) {
            throw new BusinessValidationException("Durak requires exactly 2 players");
        }

        BigDecimal bet1 = bets.get(0).getBet();
        BigDecimal bet2 = bets.get(1).getBet();

        if (bet1.compareTo(bet2) != 0) {
            log.warn("Unequal bets detected: {} vs {}", bet1, bet2);
            throw new BusinessValidationException(String.format("Bets must be equal. Player 1: %s, Player 2: %s", bet1, bet2));
        }
    }

    private void validateSufficientBalance(List<PlayerBet> bets) {
        bets.forEach(bet -> {
            if (bet.getBalanceBefore().compareTo(bet.getBet()) < 0) {
                log.warn("Insufficient balance for player {}: balance={}, bet={}", bet.getGuid(), bet.getBalanceBefore(), bet.getBet());
                throw new BusinessValidationException(String.format(
                        "Insufficient balance. Player %s: balance=%s, bet=%s",
                        bet.getGuid(),
                        bet.getBalanceBefore(),
                        bet.getBet()
                ));
            }
        });
    }

    private void validateWinnerExists(java.util.UUID winner, List<PlayerBet> bets) {
        boolean winnerExists = bets.stream()
                .anyMatch(bet -> bet.getGuid().equals(winner));

        if (!winnerExists) {
            log.warn("Winner {} not found in player bets", winner);
            throw new BusinessValidationException(String.format("Winner %s not found in player list", winner));
        }
    }

    private void validateNoOverflow(List<PlayerBet> bets) {
        BigDecimal totalPot = bets.stream()
                .map(PlayerBet::getBet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (PlayerBet bet : bets) {
            BigDecimal maxPossibleBalance = bet.getBalanceBefore().subtract(bet.getBet()).add(totalPot);

            if (maxPossibleBalance.compareTo(MAX_BALANCE) > 0) {
                log.warn("Potential overflow for player {}: max possible balance would be {}", bet.getGuid(), maxPossibleBalance);
                throw new BusinessValidationException(String.format(
                        "Balance would exceed maximum limit. Player %s: current=%s, max_possible=%s, limit=%s",
                        bet.getGuid(),
                        bet.getBalanceBefore(),
                        maxPossibleBalance,
                        MAX_BALANCE
                ));
            }
        }
    }
}
