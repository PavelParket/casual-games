package com.game_service.tic_tac_toe.exception;

public class InvalidMoveException extends GameException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
