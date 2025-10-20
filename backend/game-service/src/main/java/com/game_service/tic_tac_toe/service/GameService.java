package com.game_service.tic_tac_toe.service;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final Random random = new Random();

    public GameResponse processStart(GameRequest request) {
        String[][] board = new String[3][3];

        if (request.players().size() != 2) {
            throw new IllegalArgumentException("Exactly two players required");
        }

        List<String> players = new ArrayList<>(request.players());

        Collections.shuffle(players, random);
        String first = players.get(0);
        String second = players.get(1);

        Map<String, String> playersSymbols = Map.of(
                first, "X",
                second, "O"
        );

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
        String[][] board = request.board();
        Integer cell = request.cell();
        String player = request.player();

        if (board == null || cell == null || player == null) {
            throw new IllegalArgumentException("Invalid request: missing fields");
        }

        int row = cell / 3;
        int col = cell % 3;

        if (row < 0 || row >= 3 || col < 0) {
            throw new IllegalArgumentException("Invalid cell index: " + cell);
        }

        if (board[row][col] != null && !board[row][col].isBlank()) {
            throw new IllegalArgumentException("Cell already occupied");
        }

        board[row][col] = request.player();

        String winner = checkWinner(board);
        String message;
        MessageType type;

        if (winner != null) {
            type = winner.equals("X") ? MessageType.WINNER_X : MessageType.WINNER_O;
            message = "Player " + player + " wins!";
        } else if (isDraw(board)) {
            type = MessageType.DRAW;
            message = "It's a draw!";
        } else {
            type = MessageType.MOVE;
            message = "Next move: " + nextPlayerSymbol(player);
        }
    }
}
