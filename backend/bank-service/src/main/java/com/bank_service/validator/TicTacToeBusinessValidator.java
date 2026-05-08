package com.bank_service.validator;

import com.bank_service.domain.enums.RoomType;
import com.casualgames.grpc.transaction.PlayerBetMessage;
import com.casualgames.grpc.transaction.TicTacToeTransactionRequest;
import com.common_utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.bank_service.config.ResourceMessageConstants.BALANCE_OVERFLOW;
import static com.bank_service.config.ResourceMessageConstants.INSUFFICIENT_BALANCE;
import static com.bank_service.config.ResourceMessageConstants.INVALID_PLAYERS_COUNT;
import static com.bank_service.config.ResourceMessageConstants.NOT_FOUND_WINNER;
import static com.bank_service.config.ResourceMessageConstants.UNEQUAL_BETS;

@Component
@Slf4j
public class TicTacToeBusinessValidator implements GameBusinessValidator<TicTacToeTransactionRequest> {

    @Override
    public RoomType getRoomType() {
        return RoomType.TIC_TAC_TOE;
    }

    @Override
    public void validate(TicTacToeTransactionRequest request) {
        List<PlayerBetMessage> bets = request.getPlayerBetsList();

        validateEqualBets(bets);
        validateSufficientBalance(bets);

        if (request.hasWinner()) {
            validateWinnerExists(request.getWinner(), bets);
        }

        validateNoOverflow(bets);
    }

    private void validateEqualBets(List<PlayerBetMessage> bets) {
        if (bets.size() != 2) {
            throw new BadRequestException(String.format(INVALID_PLAYERS_COUNT, "TicTacToe"));
        }

        BigDecimal bet1 = new BigDecimal(bets.get(0).getBet());
        BigDecimal bet2 = new BigDecimal(bets.get(1).getBet());

        if (bet1.compareTo(bet2) != 0) {
            log.warn("Unequal bets detected: {} vs {}", bet1, bet2);

            throw new BadRequestException(String.format(UNEQUAL_BETS, bet1, bet2));
        }
    }

    private void validateSufficientBalance(List<PlayerBetMessage> bets) {
        for (PlayerBetMessage bet : bets) {
            BigDecimal balance = new BigDecimal(bet.getBalanceBefore());
            BigDecimal betAmount = new BigDecimal(bet.getBet());

            if (balance.compareTo(betAmount) < 0) {
                UUID guid = UUID.fromString(bet.getGuid());
                log.warn("Insufficient balance for player {}: balance={}, bet={}", guid, balance, betAmount);
                throw new BadRequestException(String.format(INSUFFICIENT_BALANCE, guid, balance, betAmount));
            }
        }
    }

    private void validateWinnerExists(String winnerStr, List<PlayerBetMessage> bets) {
        UUID winner = UUID.fromString(winnerStr);
        boolean winnerExists = bets.stream()
                .anyMatch(bet -> UUID.fromString(bet.getGuid()).equals(winner));

        if (!winnerExists) {
            log.warn("Winner {} not found in player bets", winner);
            throw new BadRequestException(NOT_FOUND_WINNER);
        }
    }

    private void validateNoOverflow(List<PlayerBetMessage> bets) {
        BigDecimal totalPot = bets.stream()
                .map(bet -> new BigDecimal(bet.getBet()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (PlayerBetMessage bet : bets) {
            BigDecimal balance = new BigDecimal(bet.getBalanceBefore());
            BigDecimal betAmount = new BigDecimal(bet.getBet());
            BigDecimal maxPossibleBalance = balance.subtract(betAmount).add(totalPot);

            if (maxPossibleBalance.compareTo(MAX_BALANCE) > 0) {
                UUID guid = UUID.fromString(bet.getGuid());

                log.warn("Potential overflow for player {}: max possible balance would be {}", guid, maxPossibleBalance);

                throw new BadRequestException(String.format(BALANCE_OVERFLOW, guid, balance, maxPossibleBalance, MAX_BALANCE));
            }
        }
    }
}
