package com.game_service.tic_tac_toe.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BoardUtils {

    private static final int SIZE = 3;

    public static boolean isCellValid(int cell) {
        return cell >= 0 && cell < SIZE * SIZE;
    }
}
