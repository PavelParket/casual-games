package com.game_service.tic_tac_toe.service;

import com.game_service.common.enums.GameType;
import com.game_service.common.enums.MessageType;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.common.exception.NotFoundException;
import com.game_service.common.service.provider.GameCleanupProvider;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.domain.entity.TicTacToe;
import com.game_service.tic_tac_toe.domain.enums.TicTacToeGameEvent;
import com.game_service.tic_tac_toe.domain.enums.TicTacToeGameStatus;
import com.game_service.tic_tac_toe.mapper.TicTacToeGameMapper;
import com.game_service.tic_tac_toe.repository.TicTacToeGameRepository;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import com.game_service.tic_tac_toe.validator.TicTacToeGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.game_service.config.ResourceMessageConstants.GAME_STARTED;
import static com.game_service.config.ResourceMessageConstants.ROOM_NOT_FOUND;
import static com.game_service.config.ResourceMessageConstants.TTT_DRAW;
import static com.game_service.config.ResourceMessageConstants.TTT_GAME_ALREADY_IN_PROGRESS;
import static com.game_service.config.ResourceMessageConstants.TTT_NEXT_PLAYER_MOVE;
import static com.game_service.config.ResourceMessageConstants.TTT_PLAYER_WINS;
import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_O;
import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_X;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicTacToeGameService implements GameCleanupProvider {

    private final TicTacToeGameValidator ticTacToeGameValidator;

    private final TicTacToeGameMapper ticTacToeGameMapper;

    private final TicTacToeGameRepository ticTacToeGameRepository;

    private final Map<UUID, TicTacToe> activeGames = new ConcurrentHashMap<>();

    private final Random random = new Random();

    @Override
    public GameType gameType() {
        return GameType.TIC_TAC_TOE;
    }

    @Override
    public void cleanup(UUID roomId) {
        TicTacToe game = activeGames.remove(roomId);

        if (game == null) {
            log.debug("forceCleanup: no active TicTacToe game for roomId={}", roomId);
            return;
        }
        
        if (game.getStatus() == TicTacToeGameStatus.ACTIVE) {
            game.setStatus(TicTacToeGameStatus.CANCELLED);
            ticTacToeGameRepository.save(game);
        }

        log.info("forceCleanup: TicTacToe game cleaned for roomId={}", roomId);
    }

    @Transactional
    public TicTacToeGameResponse processStart(TicTacToeGameRequest request) {
        log.info("Starting new game request for room {}", request.roomId());

        ticTacToeGameValidator.validateStart(request);

        if (ticTacToeGameRepository.findByRoomId(request.roomId()).isPresent()) {
            throw new InvalidMoveException(TTT_GAME_ALREADY_IN_PROGRESS);
        }

        List<UUID> players = new ArrayList<>(request.players().keySet());
        Collections.shuffle(players, random);

        UUID playerXId = players.get(0);
        UUID playerOId = players.get(1);

        String[] board = new String[9];

        TicTacToe newGame = TicTacToe.builder()
                .roomId(request.roomId())
                .playerXId(playerXId)
                .playerOId(playerOId)
                .currentTurnUserId(playerXId)
                .players(request.players())
                .board(board)
                .status(TicTacToeGameStatus.ACTIVE)
                .build();

        TicTacToe existing = activeGames.putIfAbsent(request.roomId(), newGame);

        if (existing != null) {
            log.warn("Race condition: Game in room {} was already started by another thread", request.roomId());
            throw new InvalidMoveException(TTT_GAME_ALREADY_IN_PROGRESS);
        }

        try {
            ticTacToeGameRepository.save(newGame);
        } catch (Exception e) {
            activeGames.remove(request.roomId());
            throw e;
        }

        Map<UUID, String> playersSymbols = Map.of(playerXId, SYMBOL_X, playerOId, SYMBOL_O);

        return ticTacToeGameMapper.toStartResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.START,
                request.roomId(),
                board,
                SYMBOL_X,
                SYMBOL_O,
                playersSymbols,
                request.players(),
                GAME_STARTED
        );
    }


    @Transactional
    public TicTacToeGameResponse processMove(TicTacToeGameRequest request) {
        TicTacToe game = activeGames.get(request.roomId());

        if (game == null) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }

        synchronized (game) {
            ticTacToeGameValidator.validateMove(request, game);

            String[] board = game.getBoard();
            int cell = request.cell();
            String currentSymbol = request.fromUserId().equals(game.getPlayerXId())
                    ? SYMBOL_X
                    : SYMBOL_O;

            board[cell] = currentSymbol;

            TicTacToeGameStatus status = TicTacToeGameUtils.checkWinner(board);
            game.setStatus(status);

            String message;
            String nextPlayerSymbol = null;
            UUID winner = null;
            TicTacToeGameEvent responseEvent;

            if (status == TicTacToeGameStatus.ACTIVE) {
                UUID nextUserId = game.getCurrentTurnUserId().equals(game.getPlayerXId())
                        ? game.getPlayerOId()
                        : game.getPlayerXId();
                game.setCurrentTurnUserId(nextUserId);

                nextPlayerSymbol = TicTacToeGameUtils.nextPlayerSymbol(currentSymbol);
                message = String.format(TTT_NEXT_PLAYER_MOVE, nextPlayerSymbol);
                responseEvent = TicTacToeGameEvent.MOVE;

            } else {
                game.setCurrentTurnUserId(null);

                if (status == TicTacToeGameStatus.DRAW) {
                    message = TTT_DRAW;
                    responseEvent = TicTacToeGameEvent.DRAW;
                } else {
                    winner = (status == TicTacToeGameStatus.WINNER_X)
                            ? game.getPlayerXId()
                            : game.getPlayerOId();
                    game.setWinnerId(winner);
                    responseEvent = (status == TicTacToeGameStatus.WINNER_X)
                            ? TicTacToeGameEvent.WINNER_X
                            : TicTacToeGameEvent.WINNER_O;
                    message = String.format(TTT_PLAYER_WINS, game.getPlayers().get(winner));
                }
            }

            if (status != TicTacToeGameStatus.ACTIVE) {
                ticTacToeGameRepository.save(game);
                activeGames.remove(request.roomId());
            }

            Map<UUID, String> playersSymbols = Map.of(game.getPlayerXId(), SYMBOL_X, game.getPlayerOId(), SYMBOL_O);

            return ticTacToeGameMapper.toMoveResponse(
                    MessageType.SYSTEM, responseEvent, request.roomId(), message, board,
                    cell, currentSymbol, nextPlayerSymbol, playersSymbols, game.getPlayers(), winner
            );
        }
    }
}
