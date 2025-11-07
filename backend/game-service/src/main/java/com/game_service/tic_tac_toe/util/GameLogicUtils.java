package com.game_service.tic_tac_toe.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameLogicUtils {

    public static String nextPlayerSymbol(String current) {
        return "X".equals(current) ? "O" : "X";
    }

    public static boolean isDraw(String[][] board) {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell == null || cell.isBlank()) {
                    return false;
                }
            }
        }
        return checkWinner(board) == null;
    }

    public static String checkWinner(String[][] board) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) {
                return board[i][0];
            }

            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) {
                return board[0][i];
            }
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
