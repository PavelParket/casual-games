package com.game_service.de_coder.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.de_coder.dto.DeCoderGameRequest;
import com.game_service.common.exception.CooldownException;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.UUID;

@Component
public class DeCoderGameValidator {

    private final Map<UUID, Long> userCooldowns;
    private static final long COOLDOWN_DURATION_MS = 5000;
    private static final long CODE_LENGTH = 10000;

    public DeCoderGameValidator(Map<UUID, Long> userCooldowns) {
        this.userCooldowns = userCooldowns;
    }

    public void validateStart(DeCoderGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room cannot be empty");
        }

        if (request.player() == null) {
            throw new GameValidationException("Player cannot be empty");
        }
    }

    public void validateMove(DeCoderGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.roomId() == null) {
                throw new GameValidationException("Room cannot be empty");
        }

        if (request.player() == null ) {
            throw new GameValidationException("Player is required to make a move");
        }

        if (request.code() == null) {
            throw new GameValidationException("Code cannot be null");
        }

        if (request.code() < 0 || request.code() > CODE_LENGTH - 1) {
            throw new GameValidationException("Code must be between 0000 and " + (CODE_LENGTH - 1));
        }

        UUID cooldownKey = request.player();
        long currentTime = System.currentTimeMillis();

        Long nextAllowedTime = userCooldowns.getOrDefault(cooldownKey, 0L);

        if (currentTime < nextAllowedTime) {
            throw new CooldownException(nextAllowedTime - currentTime);
        }

        userCooldowns.put(cooldownKey, currentTime + COOLDOWN_DURATION_MS);
    }

    public void validateGetState(UUID roomId) {
        if (roomId == null) {
            throw new GameValidationException("Room ID cannot be empty");
        }
    }

    public void validateGameExists(UUID roomId, Map<UUID, String> secretCodes) {
        String secretCode = secretCodes.get(roomId);
        if (secretCode == null) {
            throw new InvalidMoveException("Game not started in this room");
        }
    }

    public void validateGameNotExists(UUID roomId, Map<UUID, String> secretCodes) {
        if (secretCodes.containsKey(roomId)) {
            throw new InvalidMoveException("Game already in progress in this room");
        }
    }

    public void validateCodeNotUsed(Integer code, BitSet state) {
        if (state.get(code)) {
            throw new InvalidMoveException("Code " + code + " already tried!");
        }
    }
}