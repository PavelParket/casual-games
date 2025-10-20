package com.game_service.tic_tac_toe.service;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.MessageType;
import com.game_service.tic_tac_toe.util.BoardUtils;
import com.game_service.tic_tac_toe.util.GameLogicUtils;
import com.game_service.tic_tac_toe.validator.GameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameValidator validator;

    private final Random random = new Random();

    public GameResponse processStart(GameRequest request) {
        validator.validateStart(request);

        String[][] board = new String[3][3];

        List<String> players = new ArrayList<>(request.players());

        Collections.shuffle(players, random);

        String first = players.get(0);
        String second = players.get(1);

        Map<String, String> playersSymbols = new HashMap<>() {{
            put(first, "X");
            put(second, "O");
        }};

        log.info("Starting new game in room '{}': {} -> X, {} -> O", request.roomName(), first, second);

        return GameResponse.builder()
                .type(MessageType.START)
                .roomName(request.roomName())
                .board(board)
                .nextPlayer("X")
                .playersSymbols(playersSymbols)
                .players(request.players())
                .build();
    }

    public GameResponse processMove(GameRequest request) {
        validator.validateMove(request);

        Integer cell = request.cell();
        int row = BoardUtils.getRow(cell);
        int col = BoardUtils.getCol(cell);

        String[][] board = request.board();
        board[row][col] = request.player();

        String player = request.player();
        String winner = GameLogicUtils.checkWinner(board);
        String nextPlayer;

        MessageType type;
        String message;

        if (winner != null) {
            type = winner.equals("X") ? MessageType.WINNER_X : MessageType.WINNER_O;
            nextPlayer = null;
            message = "Player " + winner + " wins!";
        } else if (GameLogicUtils.isDraw(board)) {
            type = MessageType.DRAW;
            nextPlayer = null;
            message = "It's a draw!";
        } else {
            type = MessageType.MOVE;
            nextPlayer = GameLogicUtils.nextPlayerSymbol(player);
            message = "Next move: " + nextPlayer;
        }

        log.info("Move: player={}, cell={}, result={}", player, cell, type);

        return GameResponse.builder()
                .type(type)
                .roomName(request.roomName())
                .board(board)
                .cell(cell)
                .player(player)
                .nextPlayer(nextPlayer)
                .winner(winner)
                .message(message)
                .build();
    }
}
