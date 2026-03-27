package com.game_service.tic_tac_toe.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.entity.TicTacToeGame;
import com.game_service.tic_tac_toe.enums.TicTacToeGameEvent;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import org.springframework.stereotype.Component;

import static com.game_service.config.ResourceMessageConstants.*;

@Component
public class TicTacToeGameValidator {

    public void validateStart(TicTacToeGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.players() == null || request.players().size() != 2) {
            throw new GameValidationException(TTT_TWO_PLAYERS_REQUIRED);
        }

        if (request.roomId() == null) {
            throw new GameValidationException(TTT_ROOM_MUST_EXIST);
        }
    }

    public void validateMove(TicTacToeGameRequest request, TicTacToeGame game) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.roomId() == null && game == null) {
            throw new GameValidationException(TTT_ROOM_MUST_EXIST);
        }

        if (request.fromUserId() == null) {
            throw new GameValidationException(TTT_PLAYER_ID_CANNOT_BE_NULL);
        }

        if (request.cell() == null) {
            throw new GameValidationException(TTT_CELL_CANNOT_BE_NULL);
        }

        if (game.getEvent() != TicTacToeGameEvent.START && game.getEvent() != TicTacToeGameEvent.MOVE) {
            throw new InvalidMoveException(TTT_GAME_ALREADY_FINISHED);
        }

        if (request.currentPlayerSymbol() == null || request.currentPlayerSymbol().isBlank()) {
            throw new GameValidationException(TTT_SYMBOL_CANNOT_BE_BLANK);
        }

        int cell = request.cell();
        if (!TicTacToeGameUtils.isCellValid(cell)) {
            throw new GameValidationException(String.format(TTT_INVALID_CELL_INDEX, cell));
        }

        String[] board = game.getBoard();
        if (board != null && board[cell] != null && !board[cell].isBlank()) {
            throw new InvalidMoveException(TTT_CELL_ALREADY_OCCUPIED);
        }

        if (!"X".equals(request.currentPlayerSymbol()) && !"O".equals(request.currentPlayerSymbol())) {
            throw new GameValidationException(String.format(TTT_UNKNOWN_PLAYER_SYMBOL, request.currentPlayerSymbol()));
        }

        String expectedSymbol = TicTacToeGameUtils.getCurrentTurnSymbol(board);
        String currentSymbol;

        if (request.fromUserId().equals(game.getPlayerXId())) {
            currentSymbol = TicTacToeGameUtils.SYMBOL_X;
        } else if (request.fromUserId().equals(game.getPlayerOId())) {
            currentSymbol = TicTacToeGameUtils.SYMBOL_O;
        } else {
            throw new InvalidMoveException(TTT_UNKNOWN_PLAYER_ID);
        }

        if (!expectedSymbol.equals(currentSymbol)) {
            throw new InvalidMoveException(TTT_WRONG_PLAYER_MOVED);
        }
    }
}
