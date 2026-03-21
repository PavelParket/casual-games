package com.game_service.de_coder.validator;

import com.game_service.common.exception.CooldownException;
import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.de_coder.dto.DeCoderGameRequest;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.game_service.config.ResourceMessageConstants.*;

@Component
@RequiredArgsConstructor
public class DeCoderGameValidator {

    private final Map<UUID, Long> userCooldowns = new ConcurrentHashMap<>();

    private static final long COOLDOWN_DURATION_MS = 2000;
    private static final long MAX_CODE_VALUE = 10000;

    public void validateStart(DeCoderGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(ROOM_CANNOT_BE_EMPTY);
        }

        if (request.player() == null) {
            throw new GameValidationException(DECODER_PLAYER_CANNOT_BE_EMPTY);
        }

        if (!DeCoderGameEvent.START.equals(request.event())) {
            throw new GameValidationException(DECODER_WRONG_EVENT);
        }
    }

    public void validateMove(DeCoderGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(ROOM_CANNOT_BE_EMPTY);
        }

        if (request.player() == null) {
            throw new GameValidationException(DECODER_PLAYER_REQUIRED_FOR_MOVE);
        }

        if (request.code() == null) {
            throw new GameValidationException(DECODER_CODE_CANNOT_BE_NULL);
        }

        if (request.code() < 0 || request.code() > MAX_CODE_VALUE - 1) {
            throw new GameValidationException(String.format(DECODER_CODE_OUT_OF_RANGE, MAX_CODE_VALUE - 1));
        }

        if (!DeCoderGameEvent.MOVE.equals(request.event())) {
            throw new GameValidationException(DECODER_WRONG_EVENT);
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
            throw new GameValidationException(ROOM_CANNOT_BE_EMPTY);
        }
    }

    public void validateGameExists(UUID roomId, Map<UUID, String> secretCodes) {
        String secretCode = secretCodes.get(roomId);
        if (secretCode == null) {
            throw new InvalidMoveException(DECODER_GAME_NOT_STARTED);
        }
    }

    public void validateGameNotExists(UUID roomId, Map<UUID, String> secretCodes) {
        if (secretCodes.containsKey(roomId)) {
            throw new InvalidMoveException(DECODER_GAME_ALREADY_IN_PROGRESS);
        }
    }
}