package com.bank_service.validator;

import com.bank_service.domain.dto.game.TicTacToeTransactionRequest;
import com.bank_service.domain.entity.PlayerBet;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.bank_service.config.ResourceMessageConstants.BALANCE_OVERFLOW;
import static com.bank_service.config.ResourceMessageConstants.INSUFFICIENT_BALANCE;
import static com.bank_service.config.ResourceMessageConstants.INVALID_PLAYERS_COUNT;
import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_WINNER;
import static com.bank_service.config.ResourceMessageConstants.UNEQUAL_BETS;

@Component
@Slf4j
public class TicTacToeBusinessValidator implements GameBusinessValidator<TicTacToeTransactionRequest> {

    @Override
    public void validate(TicTacToeTransactionRequest request) {
        List<PlayerBet> bets = request.playerBets();

        validateEqualBets(bets);
        validateSufficientBalance(bets);

        if (!request.isDraw()) {
            validateWinnerExists(request.winner(), bets);
        }

        validateNoOverflow(bets);
    }

    private void validateEqualBets(List<PlayerBet> bets) {
        if (bets.size() != 2) {
            throw new BadRequestException(String.format(INVALID_PLAYERS_COUNT, "TicTacToe"));
        }

        BigDecimal bet1 = bets.get(0).getBet();
        BigDecimal bet2 = bets.get(1).getBet();

        if (bet1.compareTo(bet2) != 0) {
            log.warn("Unequal bets detected: {} vs {}", bet1, bet2);

            throw new BadRequestException(String.format(UNEQUAL_BETS, bet1, bet2));
        }
    }

    private void validateSufficientBalance(List<PlayerBet> bets) {
        for (PlayerBet bet : bets) {
            if (bet.getBalanceBefore().compareTo(bet.getBet()) < 0) {
                log.warn("Insufficient balance for player {}: balance={}, bet={}", bet.getGuid(), bet.getBalanceBefore(), bet.getBet());

                throw new BadRequestException(String.format(INSUFFICIENT_BALANCE, bet.getGuid(), bet.getBalanceBefore(), bet.getBet()));
            }
        }
    }

    private void validateWinnerExists(java.util.UUID winner, List<PlayerBet> bets) {
        boolean winnerExists = bets.stream()
                .anyMatch(bet -> bet.getGuid().equals(winner));

        if (!winnerExists) {
            log.warn("Winner {} not found in player bets", winner);

            throw new BadRequestException(NOT_FOUND_WINNER);
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

                throw new BadRequestException(String.format(BALANCE_OVERFLOW, bet.getGuid(), bet.getBalanceBefore(), maxPossibleBalance, MAX_BALANCE));
            }
        }
    }
}
