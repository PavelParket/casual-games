package com.game_service.de_coder.service;

import com.game_service.common.enums.MessageType;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.de_coder.dto.DeCoderGameRequest;
import com.game_service.de_coder.dto.DeCoderGameResponse;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import com.game_service.de_coder.mapper.DeCoderGameMapper;
import com.game_service.de_coder.util.DeCoderGameLogicUtils;
import com.game_service.de_coder.validator.DeCoderGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.BitSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeCoderGameService {

    private final DeCoderGameValidator deCoderGameValidator;

    private final DeCoderGameMapper deCoderGameMapper;

    private final Map<UUID, String> secretCodes = new ConcurrentHashMap<>();

    private final Map<UUID, BitSet> roomState = new ConcurrentHashMap<>();

    private static final int BITSET_SIZE = 10_000;

    public DeCoderGameResponse processStart(DeCoderGameRequest request) {
        deCoderGameValidator.validateStart(request);

        deCoderGameValidator.validateGameNotExists(request.roomId(), secretCodes);

        String newCode = DeCoderGameLogicUtils.generateSecretCode();
        String existingCode = secretCodes.putIfAbsent(request.roomId(), newCode);

        if (existingCode != null) {
            throw new InvalidMoveException("Game already in progress in this room");
        }

        roomState.put(request.roomId(), new BitSet(BITSET_SIZE));

        log.info("Starting new DE_CODER game in room '{}'. Secret code generated", request.roomId());

        return deCoderGameMapper.toStartResponse(
                MessageType.SYSTEM,
                DeCoderGameEvent.START,
                request.roomId(),
                "Game started!",
                request.player());
    }

    public DeCoderGameResponse processMove(DeCoderGameRequest request) {
        deCoderGameValidator.validateMove(request);

        deCoderGameValidator.validateGameExists(request.roomId(), secretCodes);

        BitSet state = roomState.computeIfAbsent(request.roomId(), k -> new BitSet(BITSET_SIZE));

        synchronized (state) {
            deCoderGameValidator.validateCodeNotUsed(request.code(), state);

            state.set(request.code());
        }

        if (DeCoderGameLogicUtils.isCodeCracked(request.code(), secretCodes.get(request.roomId()))) {

            log.info("Player {} found the code in room {}!", request.player(), request.roomId());

            secretCodes.remove(request.roomId());
            roomState.remove(request.roomId());

            return deCoderGameMapper.toWinResponse(MessageType.SYSTEM,
                    DeCoderGameEvent.WINNER,
                    request.roomId(),
                    "Player wins!",
                    request.player());
        }

        return deCoderGameMapper.toMoveResponse(MessageType.SYSTEM,
                DeCoderGameEvent.MOVE,
                request.roomId(),
                "Does not match the winning code",
                request.code(),
                request.player());
    }

    public DeCoderGameResponse getGameState(UUID roomId) {
        deCoderGameValidator.validateGetState(roomId);

        boolean isStarted = secretCodes.containsKey(roomId);

        BitSet state = roomState.get(roomId);
        String base64 = "";

        if (state != null) {
            synchronized (state) {
                byte[] bytes = state.toByteArray();
                if (bytes.length > 0) {
                    base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                }
            }
        }

        return DeCoderGameResponse.builder()
                .isGameStarted(isStarted)
                .gameState(base64)
                .build();
    }
}
