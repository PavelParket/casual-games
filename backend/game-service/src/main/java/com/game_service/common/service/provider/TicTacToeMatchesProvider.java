package com.game_service.common.service.provider;

import com.game_service.common.dto.GameMatchRequestFilter;
import com.game_service.common.dto.GameMatchResponse;
import com.game_service.common.enums.GameResult;
import com.game_service.common.enums.GameType;
import com.game_service.tic_tac_toe.domain.entity.TicTacToe;
import com.game_service.tic_tac_toe.mapper.TicTacToeGameMapper;
import com.game_service.tic_tac_toe.repository.TicTacToeGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeMatchesProvider implements GameMatchesProvider {

    private final TicTacToeGameRepository ticTacToeGameRepository;

    private final TicTacToeGameMapper ticTacToeGameMapper;

    @Override
    public GameType gameType() {
        return GameType.TIC_TAC_TOE;
    }

    @Override
    public Page<GameMatchResponse> findMatches(UUID userGuid, GameMatchRequestFilter gameMatchRequestFilter, Pageable pageable) {
        Page<TicTacToe> page = ticTacToeGameRepository.findMatchHistory(userGuid, gameMatchRequestFilter.isWinner(), pageable);

        return page.map(ticTacToe ->
                ticTacToeGameMapper.toMatchResponse(
                        ticTacToe,
                        userGuid,
                        GameType.TIC_TAC_TOE,
                        resolveGameResult(ticTacToe, userGuid)
                )
        );
    }

    private GameResult resolveGameResult(TicTacToe ticTacToe, UUID userGuid) {
        switch (ticTacToe.getStatus()) {
            case DRAW -> {
                return GameResult.DRAW;
            }

            case WINNER_O, WINNER_X -> {
                return userGuid.equals(ticTacToe.getWinnerId()) ? GameResult.WIN : GameResult.LOSS;
            }

            default -> {
                return GameResult.NO_DATA;
            }
        }
    }
}
