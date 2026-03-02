package com.game_service.tic_tac_toe.service;

import com.game_service.common.enums.MessageType;
import com.game_service.common.exception.GameValidationException;
import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.dto.TicTacToeGameResponse;
import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import com.game_service.tic_tac_toe.mapper.TicTacToeGameMapper;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import com.game_service.tic_tac_toe.validator.TicTacToeGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_O;
import static com.game_service.tic_tac_toe.util.TicTacToeGameUtils.SYMBOL_X;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicTacToeGameService {

    private final TicTacToeGameValidator ticTacToeGameValidator;

    private final TicTacToeGameMapper ticTacToeGameMapper;

    private final Random random = new Random();

    public TicTacToeGameResponse processStart(TicTacToeGameRequest request) {
        log.info("Received message {}", request);

        ticTacToeGameValidator.validateStart(request);

        String[] board = new String[9];

        List<UUID> players = new ArrayList<>(request.players().keySet());

        Collections.shuffle(players, random);

        Map<UUID, String> playersSymbols = Map.of(
                players.get(0), SYMBOL_X,
                players.get(1), SYMBOL_O
        );

        String message = "Game started!";

        log.info("Starting new game in room '{}': {}=X, {}=O", request.roomId(), players.get(0), players.get(1));

        return ticTacToeGameMapper.toStartResponse(
                MessageType.SYSTEM,
                TicTacToeGameEvent.START,
                request.roomId(),
                board,
                playersSymbols.get(players.get(0)),
                playersSymbols.get(players.get(1)),
                playersSymbols,
                request.players(),
                message
        );
    }

    /* todo: синхронизировать по комнате или скорее игре, то есть добавить состояние и по нему блокировать
        иначе два запроса могут попасть на обработку одновременно, так как вебсокеты принимают запрос и прокидывают его без блокировки
        в целом надо добавить объекты для состояний и по ним работать */
    public TicTacToeGameResponse processMove(TicTacToeGameRequest request) {
        ticTacToeGameValidator.validateMove(request);

        String[] board = request.board();
        Integer cell = request.cell();
        String currentPlayerSymbol = request.currentPlayerSymbol();

        board[cell] = currentPlayerSymbol;

        TicTacToeGameEvent event = TicTacToeGameUtils.checkWinner(board);
        String message;
        String nextPlayerSymbol = null;
        UUID winner = null;

        if (TicTacToeGameEvent.WINNER_X.equals(event) || TicTacToeGameEvent.WINNER_O.equals(event)) {
            String winnerSymbol = TicTacToeGameUtils.getWinnerSymbol(event);
            winner = request.playersSymbols().entrySet().stream()
                    .filter(playerId -> playerId.getValue().equals(winnerSymbol))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() ->
                            new GameValidationException("Winner symbol exists but player not found")
                    );

            message = "Player " + request.players().get(winner) + " wins!";
        } else if (TicTacToeGameEvent.DRAW.equals(event)) {
            message = "It's a draw!";
        } else {
            nextPlayerSymbol = TicTacToeGameUtils.nextPlayerSymbol(currentPlayerSymbol);
            message = "Player with symbol " + nextPlayerSymbol + " move now.";
        }

        return ticTacToeGameMapper.toMoveResponse(
                MessageType.SYSTEM,
                event,
                request.roomId(),
                message,
                board,
                cell,
                currentPlayerSymbol,
                nextPlayerSymbol,
                request.playersSymbols(),
                request.players(),
                winner
        );
    }
}
