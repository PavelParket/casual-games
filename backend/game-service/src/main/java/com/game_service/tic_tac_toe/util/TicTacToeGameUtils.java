package com.game_service.tic_tac_toe.util;

import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TicTacToeGameUtils {

    private static final int SIZE = 3;

    public static boolean isCellValid(int cell) {
        return cell >= 0 && cell < SIZE * SIZE;
    }

    public static String nextPlayerSymbol(String current) {
        return "X".equals(current) ? "O" : "X";
    }

    public static boolean isDraw(String[] board) {
        for (String cell : board) {
            if (cell == null || cell.isBlank()) {
                return false;
            }
        }
        return true;
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

        return TicTacToeGameEvent.MOVE;
    }

    private static TicTacToeGameEvent setWinner(String winnerSymbol, String[] board) {
        if ("X".equals(winnerSymbol)) {
            return TicTacToeGameEvent.WINNER_X;
        } else if ("O".equals(winnerSymbol)) {
            return TicTacToeGameEvent.WINNER_O;
        } else if (isDraw(board)) {
            return TicTacToeGameEvent.DRAW;
        } else {
            return null;
        }
    }

    public static String getWinnerSymbol(TicTacToeGameEvent event) {
        if (TicTacToeGameEvent.WINNER_X.equals(event)) {
            return "X";
        } else if (TicTacToeGameEvent.WINNER_O.equals(event)) {
            return "O";
        }

        return null;
    }
}
