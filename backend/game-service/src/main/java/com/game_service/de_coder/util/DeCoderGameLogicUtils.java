package com.game_service.de_coder.util;

import java.util.Random;

public class DeCoderGameLogicUtils {

    private static final int CODE_LENGTH = 4;
    private static final int MAX_VALUE = 10_000;
    private static final Random RANDOM = new Random();

    public static String generateSecretCode() {
        int codeInt = RANDOM.nextInt(MAX_VALUE);
        System.out.println(codeInt);
        return String.format("%0" + CODE_LENGTH + "d", codeInt);

    }

    public static boolean isCodeCracked(Integer code, String secretCode) {
        if (code == null) return false;

        String guessString = String.format("%0" + CODE_LENGTH + "d", code);

        return guessString.equals(secretCode);
    }
}