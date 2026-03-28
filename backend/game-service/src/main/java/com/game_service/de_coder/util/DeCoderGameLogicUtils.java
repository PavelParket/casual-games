package com.game_service.de_coder.util;

import com.game_service.de_coder.dto.DeCoderGameResponse.GameState;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class DeCoderGameLogicUtils {

    public static final int CODE_LENGTH = 4;
    private static final int ALPHABET_SIZE = 26;
    private static final Random RANDOM = new Random();

    public static String generateSecretCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            char randomChar = (char) ('A' + RANDOM.nextInt(ALPHABET_SIZE));
            sb.append(randomChar);
        }

        String code = sb.toString();
        log.info("Generated secret code for new game: {}", code);
        return code;
    }

    public static GameState calculateResult(String guessCode, String secretCode) {
        if (guessCode == null || guessCode.length() != CODE_LENGTH) {
            return new GameState(null, 0, 0);
        }

        int exactMatch = 0;
        int partialMatch = 0;

        int[] secretCodeCounts = new int[ALPHABET_SIZE];
        int[] guessCodeCounts = new int[ALPHABET_SIZE];

        for (int i = 0; i < CODE_LENGTH; i++) {
            char secretChar = secretCode.charAt(i);
            char guessChar = guessCode.charAt(i);

            if (secretChar == guessChar) {
                exactMatch++;
            } else {
                secretCodeCounts[secretChar - 'A']++;
                guessCodeCounts[guessChar - 'A']++;
            }
        }

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            partialMatch += Math.min(secretCodeCounts[i], guessCodeCounts[i]);
        }

        return new GameState(guessCode, exactMatch, partialMatch);
    }

    public static boolean isCodeCracked(GameState gameState) {
        return gameState != null && gameState.exactMatch() != null && gameState.exactMatch() == CODE_LENGTH;
    }
}