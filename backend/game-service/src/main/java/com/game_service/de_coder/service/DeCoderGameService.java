package com.game_service.de_coder.service;

import com.game_service.de_coder.dto.DeCoderGameRequest;
import com.game_service.de_coder.dto.DeCoderGameResponse;
import com.game_service.de_coder.enums.DeCoderGameEvent;
import com.game_service.de_coder.exception.InvalidMoveException;
import com.game_service.de_coder.mapper.DeCoderGameMapper;
import com.game_service.de_coder.util.DeCoderGameLogicUtils;
import com.game_service.de_coder.validator.DeCoderGameValidator;
import com.game_service.tic_tac_toe.enums.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public DeCoderGameResponse processStart(DeCoderGameRequest request) {
        deCoderGameValidator.validateStart(request);

        String newCode = DeCoderGameLogicUtils.generateSecretCode();
        String existingCode = secretCodes.putIfAbsent(request.roomId(), newCode);

        if (existingCode != null) {
            throw new InvalidMoveException("Game already in progress in this room");
        }

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

        String secretCode = secretCodes.get(request.roomId());
        if (secretCode == null) {
            throw new InvalidMoveException("Game not started in this room");
        }

        if (DeCoderGameLogicUtils.isCodeCracked(request.code(), secretCode)) {

            log.info("Player {} found the code in room {}!", request.player(), request.roomId());

            secretCodes.remove(request.roomId());

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
}
