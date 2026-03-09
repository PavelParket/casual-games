package com.game_service.de_coder.validator;

import com.game_service.common.exception.CooldownException;
import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.de_coder.dto.DeCoderGameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.UUID;

import static com.game_service.config.ResourceMessageConstants.DECODER_CODE_ALREADY_TRIED;
import static com.game_service.config.ResourceMessageConstants.DECODER_CODE_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.DECODER_CODE_OUT_OF_RANGE;
import static com.game_service.config.ResourceMessageConstants.DECODER_GAME_ALREADY_IN_PROGRESS;
import static com.game_service.config.ResourceMessageConstants.DECODER_GAME_NOT_STARTED;
import static com.game_service.config.ResourceMessageConstants.DECODER_PLAYER_CANNOT_BE_EMPTY;
import static com.game_service.config.ResourceMessageConstants.DECODER_PLAYER_REQUIRED_FOR_MOVE;
import static com.game_service.config.ResourceMessageConstants.REQUEST_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.ROOM_CANNOT_BE_EMPTY;

@Component
@RequiredArgsConstructor
public class DeCoderGameValidator {

    private static final long COOLDOWN_DURATION_MS = 5000;
    private static final long CODE_LENGTH = 10000;

    private final Map<UUID, Long> userCooldowns;

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

        if (request.code() < 0 || request.code() > CODE_LENGTH - 1) {
            throw new GameValidationException(String.format(DECODER_CODE_OUT_OF_RANGE, CODE_LENGTH - 1));
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
        if (secretCodes.get(roomId) == null) {
            throw new InvalidMoveException(DECODER_GAME_NOT_STARTED);
        }
    }

    public void validateGameNotExists(UUID roomId, Map<UUID, String> secretCodes) {
        if (secretCodes.containsKey(roomId)) {
            throw new InvalidMoveException(DECODER_GAME_ALREADY_IN_PROGRESS);
        }
    }

    public void validateCodeNotUsed(Integer code, BitSet state) {
        if (state.get(code)) {
            throw new InvalidMoveException(String.format(DECODER_CODE_ALREADY_TRIED, code));
        }
    }
}