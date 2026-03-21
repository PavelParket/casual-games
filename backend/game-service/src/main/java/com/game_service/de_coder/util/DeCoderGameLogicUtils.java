package com.game_service.de_coder.util;

import com.game_service.de_coder.dto.DeCoderGameResponse.GameState;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class DeCoderGameLogicUtils {

    private static final int CODE_LENGTH = 4;
    private static final int MAX_VALUE = 10_000;
    private static final Random RANDOM = new Random();

    public static String generateSecretCode() {
        int codeInt = RANDOM.nextInt(MAX_VALUE);
        log.info("Generated secret code for new game: {}", codeInt);
        return String.format("%0" + CODE_LENGTH + "d", codeInt);

    }

    public static GameState calculateResult(Integer guessCode, String secretCode) {
        if (guessCode == null) {
            return new GameState(null, 0, 0);
        }

        String guessString = String.format("%0" + CODE_LENGTH + "d", guessCode);

        int exactMatch = 0;
        int partialMatch = 0;

        int[] secretCodeCounts = new int[10];
        int[] guessCodeCounts = new int[10];

        for (int i = 0; i < CODE_LENGTH; i++) {
            char secretCodeDigit = secretCode.charAt(i);
            char guessCodeDigit = guessString.charAt(i);

            if (secretCodeDigit == guessCodeDigit) {
                exactMatch++;
            } else {
                secretCodeCounts[secretCodeDigit  - '0']++;
                guessCodeCounts[guessCodeDigit - '0']++;
            }
        }

        for (int i = 0; i < 10; i++) {
            partialMatch += Math.min(secretCodeCounts[i], guessCodeCounts[i]);
        }

        return new GameState(guessCode, exactMatch, partialMatch);
    }

    public static boolean isCodeCracked(GameState gameState) {
        return gameState != null && gameState.exactMatch() != null && gameState.exactMatch() == CODE_LENGTH;
    }
}