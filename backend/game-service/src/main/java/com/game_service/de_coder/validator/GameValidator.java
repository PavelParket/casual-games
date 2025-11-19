package com.game_service.de_coder.validator;

import com.game_service.de_coder.dto.GameRequest;
import com.game_service.de_coder.exception.GameValidationException;
import com.game_service.de_coder.exception.CooldownException;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component("deCoderValidator")
public class GameValidator {

    private final Map<String, Long> userCooldowns;
    private static final long COOLDOWN_DURATION_MS = 10000;

    public GameValidator(Map<String, Long> userCooldowns) {
        this.userCooldowns = userCooldowns;
    }

    public void validateStart(GameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.players() == null) {
            throw new GameValidationException("Exactly one players required");
        }

        if (request.roomName() == null || request.roomName().isBlank()) {
            throw new GameValidationException("Room name cannot be empty");
        }
    }

    public void validateMove(GameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomName() == null || request.roomName().isBlank()) {
            throw new GameValidationException("Room name cannot be empty");
        }

        if (request.player() == null || request.player().isBlank()) {
            throw new GameValidationException("Player is required to make a move");
        }

        if (request.code() == null) {
            throw new GameValidationException("Code cannot be null");
        }

        if (request.players() == null) {
            throw new GameValidationException("Exactly one players required");
        }

        if (request.code() < 0 || request.code() > 999999) {
            throw new GameValidationException("Code must be between 000000 and 999999");
        }



        String activePlayer = request.player();
        long currentTime = System.currentTimeMillis();

        Long nextAllowedTime = userCooldowns.getOrDefault(activePlayer, 0L);

        if (currentTime < nextAllowedTime) {
            long remainingTimeMs = nextAllowedTime - currentTime;
            throw new CooldownException(remainingTimeMs);
        }

        long newCooldownTime = currentTime + COOLDOWN_DURATION_MS;
        userCooldowns.put(activePlayer, newCooldownTime);
    }
}