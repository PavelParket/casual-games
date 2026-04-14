package com.game_service.de_coder.service;

import com.game_service.common.enums.GameType;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.common.service.provider.GameCleanupProvider;
import com.game_service.de_coder.domain.dto.DeCoderGameRequest;
import com.game_service.de_coder.domain.dto.DeCoderGameResponse;
import com.game_service.de_coder.domain.entity.DeCoderGameState;
import com.game_service.de_coder.domain.enums.DeCoderGameEvent;
import com.game_service.de_coder.mapper.DeCoderGameMapper;
import com.game_service.de_coder.util.DeCoderGameLogicUtils;
import com.game_service.de_coder.validator.DeCoderGameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.game_service.config.ResourceMessageConstants.DECODER_GAME_ALREADY_IN_PROGRESS;
import static com.game_service.config.ResourceMessageConstants.GAME_STARTED;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeCoderGameService implements GameCleanupProvider {

    private final DeCoderGameValidator deCoderGameValidator;

    private final DeCoderGameMapper deCoderGameMapper;

    private final Map<UUID, String> secretCodes = new ConcurrentHashMap<>();

    private final Map<UUID, Map<String, DeCoderGameState>> roomState = new ConcurrentHashMap<>();

    private final Map<UUID, BigDecimal> jackpots = new ConcurrentHashMap<>();

    private static final BigDecimal JACKPOT_INCREMENT = new BigDecimal("8.00");

    @Override
    public GameType gameType() {
        return GameType.DE_CODER;
    }

    @Override
    public void cleanup(UUID roomId) {
        secretCodes.remove(roomId);
        roomState.remove(roomId);
        jackpots.remove(roomId);

        log.info("forceCleanup: DeCoder game cleaned for roomId={}", roomId);
    }

    public DeCoderGameResponse processStart(DeCoderGameRequest request) {
        deCoderGameValidator.validateStart(request);

        deCoderGameValidator.validateGameNotExists(request.roomId(), secretCodes);

        String newCode = DeCoderGameLogicUtils.generateSecretCode();
        String existingCode = secretCodes.putIfAbsent(request.roomId(), newCode);

        if (existingCode != null) {
            throw new InvalidMoveException(DECODER_GAME_ALREADY_IN_PROGRESS);
        }

        roomState.put(request.roomId(), new LinkedHashMap<>());
        jackpots.put(request.roomId(), new BigDecimal("100.00"));

        log.info("Starting new DE_CODER game in room '{}'. Secret code generated", request.roomId());

        return deCoderGameMapper.toStartResponse(
                DeCoderGameEvent.START,
                request.roomId(),
                GAME_STARTED);
    }

    public DeCoderGameResponse processMove(DeCoderGameRequest request) {
        deCoderGameValidator.validateMove(request);

        deCoderGameValidator.validateGameExists(request.roomId(), secretCodes);

        Map<String, DeCoderGameState> history = roomState.get(request.roomId());
        if (history == null) {
            throw new InvalidMoveException("Game state not found");
        }

        BigDecimal currentJackpot = jackpots.merge(request.roomId(), JACKPOT_INCREMENT, BigDecimal::add);

        DeCoderGameState moveResult;

        synchronized (history) {
            if (history.containsKey(request.code())) {
                moveResult = history.get(request.code());
            } else {
                moveResult = DeCoderGameLogicUtils.calculateResult(request.code(), secretCodes.get(request.roomId()));
                history.put(request.code(), moveResult);
            }
        }

        if (DeCoderGameLogicUtils.isCodeCracked(moveResult)) {
            log.info("Player {} found the code in room {}!", request.player(), request.roomId());

            secretCodes.remove(request.roomId());
            roomState.remove(request.roomId());
            jackpots.remove(request.roomId());

            return deCoderGameMapper.toWinResponse(
                    DeCoderGameEvent.WINNER,
                    request.roomId(),
                    "Player wins!",
                    currentJackpot,
                    request.player());
        }

        return deCoderGameMapper.toMoveResponse(
                DeCoderGameEvent.MOVE,
                request.roomId(),
                "Does not match the winning code",
                List.of(moveResult),
                request.player());
    }

    public DeCoderGameResponse getGameState(UUID roomId) {
        deCoderGameValidator.validateGetState(roomId);

        boolean isStarted = secretCodes.containsKey(roomId);

        Map<String, DeCoderGameState> history = roomState.get(roomId);
        List<DeCoderGameState> historyList = new ArrayList<>();

        if (history != null) {
            synchronized (history) {
                historyList.addAll(history.values());
            }
        }

        return DeCoderGameResponse.builder()
                .event(DeCoderGameEvent.STATE)
                .roomId(roomId)
                .isGameStarted(isStarted)
                .gameState(historyList)
                .build();
    }
}
