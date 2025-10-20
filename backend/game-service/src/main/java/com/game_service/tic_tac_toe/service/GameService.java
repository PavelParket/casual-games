package com.game_service.tic_tac_toe.service;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.MessageType;
import com.game_service.tic_tac_toe.exception.GameValidationException;
import com.game_service.tic_tac_toe.exception.InvalidMoveException;
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
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.players() == null || request.players().size() != 2) {
            throw new GameValidationException("Exactly two players required");
        }

        String[][] board = new String[3][3];

        List<String> players = new ArrayList<>(request.players());

        Collections.shuffle(players, random);
        String first = players.get(0);
        String second = players.get(1);

        Map<String, String> playersSymbols = Map.of(
                first, "X",
                second, "O"
        );

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
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        String[][] board = request.board();
        Integer cell = request.cell();
        String player = request.player();

        if (board == null || cell == null || player == null) {
            throw new GameValidationException("Invalid request: missing board, cell or player");
        }

        int row = cell / 3;
        int col = cell % 3;

        if (row < 0 || row >= 3 || col < 0) {
            throw new GameValidationException("Invalid cell index: " + cell);
        }

        if (board[row][col] != null && !board[row][col].isBlank()) {
            throw new InvalidMoveException("Cell already occupied");
        }

        if (!player.equals("X") && !player.equals("O")) {
            throw new GameValidationException("Unknown player: " + player);
        }

        board[row][col] = request.player();

        MessageType type;
        String nextPlayer;
        String winner = checkWinner(board);
        String message;

        if (winner != null) {
            type = winner.equals("X") ? MessageType.WINNER_X : MessageType.WINNER_O;
            nextPlayer = null;
            message = "Player " + player + " wins!";
        } else if (isDraw(board)) {
            type = MessageType.DRAW;
            nextPlayer = null;
            message = "It's a draw!";
        } else {
            type = MessageType.MOVE;
            nextPlayer = nextPlayerSymbol(player);
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

    private String nextPlayerSymbol(String current) {
        return current.equals("X") ? "O" : "X";
    }

    private boolean isDraw(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell == null || cell.isBlank()) {
                    return false;
                }
            }
        }

        return checkWinner(board) == null;
    }

    private String checkWinner(String[][] board) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                return board[i][0];
            }
        }

        for (int i = 0; i < 3; i++) {
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i]))
                return board[0][i];
        }

        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) {
            return board[0][0];
        }

        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) {
            return board[0][2];
        }

        return null;
    }
}
