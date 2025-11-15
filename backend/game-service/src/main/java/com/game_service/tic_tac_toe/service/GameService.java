package com.game_service.tic_tac_toe.service;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.dto.GameResponse;
import com.game_service.tic_tac_toe.enums.GameEvent;
import com.game_service.tic_tac_toe.mapper.GameMapper;
import com.game_service.tic_tac_toe.util.GameLogicUtils;
import com.game_service.tic_tac_toe.validator.GameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameValidator validator;

    private final GameMapper mapper;

    private final Random random = new Random();

    public GameResponse processStart(GameRequest request) {
        log.info("Received message {}", request);

        validator.validateStart(request);

        String[] board = new String[9];

        List<String> players = new ArrayList<>(request.players());

        Collections.shuffle(players, random);

        Map<String, String> playersSymbols = Map.of(
                players.get(0), "X",
                players.get(1), "O"
        );

        log.info("Starting new game in room '{}': {}=X, {}=O", request.roomName(), players.get(0), players.get(1));

        return mapper.toStartResponse(request, board, playersSymbols, request.players());
    }

    public GameResponse processMove(GameRequest request) {
        validator.validateMove(request);

        Integer cell = request.cell();

        String player = request.player();

        String[] board = request.board();

        board[cell] = player;

        String winner = GameLogicUtils.checkWinner(board);

        if (winner != null) {
            GameEvent event = winner.equals("X") ? GameEvent.WINNER_X : GameEvent.WINNER_O;
            return mapper.toWinResponse(request, event, board, cell, player, winner);
        } else if (GameLogicUtils.isDraw(board)) {
            return mapper.toDrawResponse(request, board, cell, player);
        } else {
            String nextPlayer = GameLogicUtils.nextPlayerSymbol(player);
            return mapper.toMoveResponse(request, board, cell, player, nextPlayer);
        }
    }
}
