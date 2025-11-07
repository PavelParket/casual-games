package com.game_service.tic_tac_toe.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardUtils {

    private static final int SIZE = 3;

    public static boolean isCellValid(int cell) {
        return cell >= 0 && cell < SIZE * SIZE;
    }

    public static int getRow(int cell) {
        return cell / SIZE;
    }

    public static int getCol(int cell) {
        return cell % SIZE;
    }
}
