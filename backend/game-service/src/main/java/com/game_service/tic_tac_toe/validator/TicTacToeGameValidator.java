package com.game_service.tic_tac_toe.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeGameValidator {

    public void validateStart(TicTacToeGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null!");
        }

        if (request.players() == null || request.players().size() != 2) {
            throw new GameValidationException("Exactly two players required!");
        }

        if (request.roomId() == null) {
            throw new GameValidationException("Room must exist!");
        }
    }

    public void validateMove(TicTacToeGameRequest request) {
        if (request == null) {
            throw new GameValidationException("Request cannot be null!");
        }

        if (request.board() == null) {
            throw new GameValidationException("Board cannot be null!");
        }

        if (request.cell() == null) {
            throw new GameValidationException("Cell index cannot be null!");
        }

        if (request.currentPlayerSymbol() == null || request.currentPlayerSymbol().isBlank()) {
            throw new GameValidationException("Player symbol cannot be null or blank!");
        }

        int cell = request.cell();
        if (!TicTacToeGameUtils.isCellValid(cell)) {
            throw new GameValidationException("Invalid cell index: " + cell);
        }

        String[] board = request.board();
        if (board[cell] != null && !board[cell].isBlank()) {
            throw new InvalidMoveException("Cell already occupied!");
        }

        if (!"X".equals(request.currentPlayerSymbol()) && !"O".equals(request.currentPlayerSymbol())) {
            throw new GameValidationException("Unknown player symbol: " + request.currentPlayerSymbol());
        }

        if (!request.playersSymbols().get(request.fromUserId()).equals(request.currentPlayerSymbol())) {
            throw new GameValidationException("Wrong player moved!");
        }
    }
}
