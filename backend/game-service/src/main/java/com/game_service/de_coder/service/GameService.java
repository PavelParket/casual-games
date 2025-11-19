package com.game_service.de_coder.service;

import com.game_service.de_coder.dto.GameRequest;
import com.game_service.de_coder.dto.GameResponse;
import com.game_service.de_coder.mapper.GameMapper;
import com.game_service.de_coder.util.GameLogicUtils;
import com.game_service.de_coder.validator.GameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service("deCoderService")
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameValidator validator;

    private final GameMapper mapper;

    private final Map<String, String> secretCodes = new ConcurrentHashMap<>();

    public GameResponse processStart(GameRequest request) {
        validator.validateStart(request);

        String newCode = GameLogicUtils.generateSecretCode();
        secretCodes.put(request.roomName(), newCode);

        Set<String> players = request.players() != null ? new HashSet<>(request.players()) : Collections.emptySet();
        log.info("Starting new DE_CODER game in room '{}'. Secret code generated", request.roomName());

        return mapper.toStartResponse(request.roomName(), players);
    }

    public GameResponse processMove(GameRequest request) {
        validator.validateMove(request);

        String secretCode = secretCodes.get(request.roomName());

        Set<String> updatedPlayers = new HashSet<>(request.players());
        updatedPlayers.add(request.player());

        if (GameLogicUtils.isCodeCracked(request.code(), secretCode)) {

            log.info("Player {} found the code in room {}!", request.player(), request.roomName());

            secretCodes.remove(request.roomName());

            return mapper.toWinResponse(request.roomName(), request.player());
        }

        return mapper.toMoveResponse(request.roomName(), request.player(), request.code(), updatedPlayers);
    }
}
