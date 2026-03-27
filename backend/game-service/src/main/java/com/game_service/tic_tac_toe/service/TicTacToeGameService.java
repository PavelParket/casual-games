package com.game_service.tic_tac_toe.service;

import com.game_service.common.enums.MessageType;
import com.game_service.common.exception.GameValidationException;
import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.entity.TicTacToeGame;
import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
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

import static com.game_service.config.ResourceMessageConstants.GAME_STARTED;
import static com.game_service.config.ResourceMessageConstants.TTT_GAME_ALREADY_IN_PROGRESS;
import static com.game_service.config.ResourceMessageConstants.TTT_DRAW;
import static com.game_service.config.ResourceMessageConstants.TTT_NEXT_PLAYER_MOVE;
import static com.game_service.config.ResourceMessageConstants.TTT_PLAYER_WINS;
import static com.game_service.config.ResourceMessageConstants.TTT_ROOM_MUST_EXIST;
import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_O;
import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_X;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicTacToeGameService {

    private final TicTacToeGameValidator ticTacToeGameValidator;

    private final TicTacToeGameMapper ticTacToeGameMapper;

    private final TicTacToeGameRepository ticTacToeGameRepository;

    private final Random random = new Random();

    @Transactional
    public TicTacToeGameResponse processStart(TicTacToeGameRequest request) {
        System.out.println(request);
        log.info("Received message {}", request);

        ticTacToeGameValidator.validateStart(request);

        if (ticTacToeGameRepository.findByRoomId(request.roomId()).isPresent()) {
            throw new GameValidationException(TTT_GAME_ALREADY_IN_PROGRESS);
        }

        String[] board = new String[9];

        List<UUID> players = new ArrayList<>(request.players().keySet());

        Collections.shuffle(players, random);

        UUID playerXId = players.get(0);
        UUID playerOId = players.get(1);

        TicTacToeGame game = TicTacToeGame.builder()
                .roomId(request.roomId())
                .playerXId(playerXId)
                .playerOId(playerOId)
                .players(request.players())
                .board(board)
                .event(TicTacToeGameEvent.START)
                .build();

        ticTacToeGameRepository.save(game);

        Map<UUID, String> playersSymbols = Map.of(
                playerXId, SYMBOL_X,
                playerOId, SYMBOL_O
        );

        log.info("Starting new game in room '{}': {}=X, {}=O", request.roomId(), playerXId, playerOId);

        return ticTacToeGameMapper.toStartResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.START,
                request.roomId(),
                board,
                playersSymbols.get(playerXId),
                playersSymbols.get(playerOId),
                playersSymbols,
                request.players(),
                GAME_STARTED
        );
    }

    @Transactional
    public TicTacToeGameResponse processMove(TicTacToeGameRequest request) {
        System.out.println(request);
        TicTacToeGame game = ticTacToeGameRepository.findByRoomId(request.roomId())
                .orElseThrow(() -> new GameValidationException(TTT_ROOM_MUST_EXIST));

        ticTacToeGameValidator.validateMove(request, game);

        String[] board = game.getBoard();

        Integer cell = request.cell();

        String currentSymbol = request.fromUserId().equals(game.getPlayerXId()) ? SYMBOL_X : SYMBOL_O;

        board[cell] = currentSymbol;
        game.setBoard(board);

        TicTacToeGameEvent event = TicTacToeGameUtils.checkWinner(board);
        game.setEvent(event);

        String message;
        String nextPlayerSymbol = null;
        UUID winner = null;

        if (TicTacToeGameEvent.WINNER_X.equals(event) || TicTacToeGameEvent.WINNER_O.equals(event)) {
            winner = TicTacToeGameEvent.WINNER_X.equals(event) ? game.getPlayerXId() : game.getPlayerOId();
            game.setWinnerId(winner);

            message = String.format(TTT_PLAYER_WINS, game.getPlayers().get(winner));;
        } else if (TicTacToeGameEvent.DRAW.equals(event)) {
            message = TTT_DRAW;
        } else {
            nextPlayerSymbol = TicTacToeGameUtils.nextPlayerSymbol(currentSymbol);
            message = String.format(TTT_NEXT_PLAYER_MOVE, nextPlayerSymbol);
        }

        ticTacToeGameRepository.save(game);

        Map<UUID, String> playersSymbols = Map.of(
                game.getPlayerXId(), SYMBOL_X,
                game.getPlayerOId(), SYMBOL_O
        );

        return ticTacToeGameMapper.toMoveResponse(
                MessageType.SYSTEM,
                event,
                request.roomId(),
                message,
                board,
                cell,
                currentSymbol,
                nextPlayerSymbol,
                playersSymbols,
                game.getPlayers(),
                winner
        );
    }
}
