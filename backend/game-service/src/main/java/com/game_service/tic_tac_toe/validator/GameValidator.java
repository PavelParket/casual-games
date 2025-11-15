package com.game_service.tic_tac_toe.validator;

import com.game_service.tic_tac_toe.dto.GameRequest;
import com.game_service.tic_tac_toe.exception.GameValidationException;
import com.game_service.tic_tac_toe.exception.InvalidMoveException;
import com.game_service.tic_tac_toe.util.BoardUtils;
import org.springframework.stereotype.Component;

@Component
public class GameValidator {

    public void validateStart(GameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.players() == null || request.players().size() != 2) {
            throw new GameValidationException("Exactly two players required");
        }

        if (request.roomName() == null || request.roomName().isBlank()) {
            throw new GameValidationException("Room name cannot be empty");
        }
    }

    public void validateMove(GameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null");
        }

        if (request.board() == null) {
            throw new GameValidationException("Board cannot be null");
        }

        if (request.cell() == null) {
            throw new GameValidationException("Cell index cannot be null");
        }

        if (request.player() == null || request.player().isBlank()) {
            throw new GameValidationException("Player symbol cannot be null or blank");
        }

        int cell = request.cell();
        if (!BoardUtils.isCellValid(cell)) {
            throw new GameValidationException("Invalid cell index: " + cell);
        }

        String[] board = request.board();

        if (board[cell] != null && !board[cell].isBlank()) {
            throw new InvalidMoveException("Cell already occupied");
        }

        if (!"X".equals(request.player()) && !"O".equals(request.player())) {
            throw new GameValidationException("Unknown player: " + request.player());
        }
    }
}
