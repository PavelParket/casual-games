package com.game_service.tic_tac_toe.util;

import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class TicTacToeGameUtils {

    private static final int SIZE = 3;
    public static final String SYMBOL_X = "X";
    public static final String SYMBOL_O = "O";

    public static boolean isCellValid(int cell) {
        return cell >= 0 && cell < SIZE * SIZE;
    }

    public static String nextPlayerSymbol(String current) {
        return "X".equals(current) ? "O" : "X";
    }

    public static boolean isDraw(String[] board) {
        return Arrays.stream(board)
                .allMatch(cell -> cell != null && !cell.isBlank());
    }

    public static TicTacToeGameEvent checkWinner(String[] board) {
        // Rows
        for (int i = 0; i < 9; i += 3) {
            if (board[i] != null && board[i].equals(board[i + 1]) && board[i + 1].equals(board[i + 2])) {
                return setWinner(board[i], board);
            }
        }

        // Columns
        for (int i = 0; i < 3; i++) {
            if (board[i] != null && board[i].equals(board[i + 3]) && board[i + 3].equals(board[i + 6])) {
                return setWinner(board[i], board);
            }
        }

        // Diagonals
        if (board[0] != null && board[0].equals(board[4]) && board[4].equals(board[8])) {
            return setWinner(board[0], board);
        }

        if (board[2] != null && board[2].equals(board[4]) && board[4].equals(board[6])) {
            return setWinner(board[2], board);
        }

        if (isDraw(board)) {
            return TicTacToeGameEvent.DRAW;
        }
        return TicTacToeGameEvent.MOVE;
    }

    private static TicTacToeGameEvent setWinner(String winnerSymbol, String[] board) {
        if (SYMBOL_X.equals(winnerSymbol)) {
            return TicTacToeGameEvent.WINNER_X;
        } else if (SYMBOL_O.equals(winnerSymbol)) {
            return TicTacToeGameEvent.WINNER_O;
        } else {
            return null;
        }
    }

    public static String getWinnerSymbol(TicTacToeGameEvent event) {
        if (TicTacToeGameEvent.WINNER_X.equals(event)) {
            return SYMBOL_X;
        } else if (TicTacToeGameEvent.WINNER_O.equals(event)) {
            return SYMBOL_O;
        }

        return null;
    }
}
