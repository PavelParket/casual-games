package com.game_service.tic_tac_toe.validator;

import com.game_service.common.exception.GameValidationException;
import com.game_service.common.exception.InvalidMoveException;
import com.game_service.tic_tac_toe.dto.TicTacToeGameRequest;
import com.game_service.tic_tac_toe.util.TicTacToeGameUtils;
import org.springframework.stereotype.Component;

import static com.game_service.config.ResourceMessageConstants.REQUEST_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.TTT_BOARD_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.TTT_CELL_ALREADY_OCCUPIED;
import static com.game_service.config.ResourceMessageConstants.TTT_CELL_CANNOT_BE_NULL;
import static com.game_service.config.ResourceMessageConstants.TTT_INVALID_CELL_INDEX;
import static com.game_service.config.ResourceMessageConstants.TTT_ROOM_MUST_EXIST;
import static com.game_service.config.ResourceMessageConstants.TTT_SYMBOL_CANNOT_BE_BLANK;
import static com.game_service.config.ResourceMessageConstants.TTT_TWO_PLAYERS_REQUIRED;
import static com.game_service.config.ResourceMessageConstants.TTT_UNKNOWN_PLAYER_SYMBOL;
import static com.game_service.config.ResourceMessageConstants.TTT_WRONG_PLAYER_MOVED;

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

    public void validateMove(TicTacToeGameRequest request) {
        if (request == null) {
            throw new GameValidationException(REQUEST_CANNOT_BE_NULL);
        }

        if (request.board() == null) {
            throw new GameValidationException(TTT_BOARD_CANNOT_BE_NULL);
        }

        if (request.cell() == null) {
            throw new GameValidationException(TTT_CELL_CANNOT_BE_NULL);
        }

        if (request.currentPlayerSymbol() == null || request.currentPlayerSymbol().isBlank()) {
            throw new GameValidationException(TTT_SYMBOL_CANNOT_BE_BLANK);
        }

        int cell = request.cell();
        if (!TicTacToeGameUtils.isCellValid(cell)) {
            throw new GameValidationException(String.format(TTT_INVALID_CELL_INDEX, cell));
        }

        String[] board = request.board();
        if (board[cell] != null && !board[cell].isBlank()) {
            throw new InvalidMoveException(TTT_CELL_ALREADY_OCCUPIED);
        }

        if (!"X".equals(request.currentPlayerSymbol()) && !"O".equals(request.currentPlayerSymbol())) {
            throw new GameValidationException(String.format(TTT_UNKNOWN_PLAYER_SYMBOL, request.currentPlayerSymbol()));
        }

        if (!request.playersSymbols().get(request.fromUserId()).equals(request.currentPlayerSymbol())) {
            throw new GameValidationException(TTT_WRONG_PLAYER_MOVED);
        }
    }
}
