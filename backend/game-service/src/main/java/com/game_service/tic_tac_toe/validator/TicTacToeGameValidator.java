package com.game_service.tic_tac_toe.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.common.exception.NotFoundException;
import com.game_service.tic_tac_toe.domain.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.domain.entity.TicTacToe;
import com.game_service.tic_tac_toe.domain.enums.TicTacToeGameStatus;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import org.springframework.stereotype.Component;

import static com.game_service.config.ResourceMessageConstants.REQUEST_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.ROOM_NOT_FOUND;
import static com.game_service.config.ResourceMessageConstants.TTT_TWO_PLAYERS_REQUIRED;
import static com.game_service.config.ResourceMessageConstants.TTT_PLAYER_ID_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.TTT_CELL_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.TTT_GAME_ALREADY_FINISHED;
import static com.game_service.config.ResourceMessageConstants.TTT_SYMBOL_CANNOT_BE_BLANK;
import static com.game_service.config.ResourceMessageConstants.TTT_INVALID_CELL_INDEX;
import static com.game_service.config.ResourceMessageConstants.TTT_CELL_ALREADY_OCCUPIED;
import static com.game_service.config.ResourceMessageConstants.TTT_WRONG_PLAYER_MOVED;
import static com.game_service.config.ResourceMessageConstants.TTT_UNKNOWN_PLAYER_ID;
import static com.game_service.config.ResourceMessageConstants.TTT_UNKNOWN_PLAYER_SYMBOL;
import static com.game_service.config.ResourceMessageConstants.TTT_WRONG_PLAYER_SYMBOL;

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
            throw new NotFoundException(ROOM_NOT_FOUND);
        }
    }

    public void validateMove(TicTacToeGameRequest request, TicTacToe game) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.roomId() == null && game == null) {
            throw new NotFoundException(ROOM_NOT_FOUND);
        }

        if (request.fromUserId() == null) {
            throw new GameValidationException(TTT_PLAYER_ID_CANNOT_BE_NULL);
        }

        if (request.cell() == null) {
            throw new GameValidationException(TTT_CELL_CANNOT_BE_NULL);
        }

        if (game.getStatus() != TicTacToeGameStatus.ACTIVE) {
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

        if (!game.getCurrentTurnUserId().equals(request.fromUserId())) {
            throw new InvalidMoveException(TTT_WRONG_PLAYER_MOVED);
        }

        if (!"X".equals(request.currentPlayerSymbol()) && !"O".equals(request.currentPlayerSymbol())) {
            throw new GameValidationException(String.format(TTT_UNKNOWN_PLAYER_SYMBOL, request.currentPlayerSymbol()));
        }

        String currentSymbol;

        if (request.fromUserId().equals(game.getPlayerXId())) {
            currentSymbol = TicTacToeGameUtils.SYMBOL_X;
        } else if (request.fromUserId().equals(game.getPlayerOId())) {
            currentSymbol = TicTacToeGameUtils.SYMBOL_O;
        } else {
            throw new InvalidMoveException(TTT_UNKNOWN_PLAYER_ID);
        }

        if (!currentSymbol.equals(request.currentPlayerSymbol())) {
            throw new InvalidMoveException(TTT_WRONG_PLAYER_SYMBOL);
        }
    }
}
