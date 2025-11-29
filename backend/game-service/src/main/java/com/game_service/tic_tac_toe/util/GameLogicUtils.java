package com.game_service.tic_tac_toe.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameLogicUtils {

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

    public static String checkWinner(String[] board) {
        // Rows
        for (int i = 0; i < 9; i += 3) {
            if (board[i] != null && board[i].equals(board[i + 1]) && board[i + 1].equals(board[i + 2])) {
                return board[i];
            }
        }

        // Columns
        for (int i = 0; i < 3; i++) {
            if (board[i] != null && board[i].equals(board[i + 3]) && board[i + 3].equals(board[i + 6])) {
                return board[i];
            }
        }

        // Diagonals
        if (board[0] != null && board[0].equals(board[4]) && board[4].equals(board[8])) {
            return board[0];
        }

        if (board[2] != null && board[2].equals(board[4]) && board[4].equals(board[6])) {
            return board[2];
        }

        return null;
    }
}
