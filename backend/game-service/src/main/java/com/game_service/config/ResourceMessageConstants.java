package com.game_service.config;

public class ResourceMessageConstants {

    public static final String REQUEST_CANNOT_BE_NULL = "Request cannot be null";
    public static final String ROOM_CANNOT_BE_EMPTY = "Room cannot be empty";
    public static final String UNEXPECTED_SERVER_ERROR = "Unexpected server error";
    public static final String UNREADABLE_REQUEST_BODY = "Malformed or unreadable request body";
    public static final String VALIDATION_FAILED = "Validation failed";

    public static final String COOLDOWN_WAIT_SECONDS = "Please wait %d seconds before trying again.";

    public static final String GAME_STARTED = "Game started!";
    public static final String GAME_TYPE_NOT_FOUND = "Game type not found: %s";

    // -------------------------------------------------------------------------
    // TicTacToe
    // -------------------------------------------------------------------------

    public static final String TTT_TWO_PLAYERS_REQUIRED = "Exactly two players required!";
    public static final String TTT_ROOM_MUST_EXIST = "Room must exist!";
    public static final String TTT_BOARD_CANNOT_BE_NULL = "Board cannot be null!";
    public static final String TTT_CELL_CANNOT_BE_NULL = "Cell index cannot be null!";
    public static final String TTT_SYMBOL_CANNOT_BE_BLANK = "Player symbol cannot be null or blank!";
    public static final String TTT_CELL_ALREADY_OCCUPIED = "Cell already occupied!";
    public static final String TTT_WRONG_PLAYER_MOVED = "Wrong player moved!";
    public static final String TTT_WINNER_PLAYER_NOT_FOUND = "Winner symbol exists but player not found";
    public static final String TTT_INVALID_CELL_INDEX = "Invalid cell index: %d";
    public static final String TTT_UNKNOWN_PLAYER_SYMBOL = "Unknown player symbol: %s";
    public static final String TTT_DRAW = "It's a draw!";
    public static final String TTT_PLAYER_WINS = "Player %s wins!";
    public static final String TTT_NEXT_PLAYER_MOVE = "Player with symbol %s move now.";

    // -------------------------------------------------------------------------
    // DeCoder
    // -------------------------------------------------------------------------

    public static final String DECODER_PLAYER_CANNOT_BE_EMPTY = "Player cannot be empty";
    public static final String DECODER_PLAYER_REQUIRED_FOR_MOVE = "Player is required to make a move";
    public static final String DECODER_CODE_CANNOT_BE_NULL = "Code cannot be null";
    public static final String DECODER_GAME_NOT_STARTED = "Game not started in this room";
    public static final String DECODER_GAME_ALREADY_IN_PROGRESS = "Game already in progress in this room";
    public static final String DECODER_WRONG_CODE_FORMAT = "Code must contains only %d uppercase letters of the latin alphabet!";
    public static final String DECODER_CODE_ALREADY_TRIED = "Code %d already tried!";
    public static final String DECODER_WRONG_EVENT = "Wrong game event";

    // -------------------------------------------------------------------------
    // HorseRace
    // -------------------------------------------------------------------------

    public static final String HORSE_RACE_NOT_FOUND = "Horse race not found id=%s";
    public static final String HORSE_RACE_ROOM_ID_CANNOT_BE_NULL = "Room id cannot be null";
    public static final String HORSE_RACE_WRONG_EVENT = "Wrong game phase";
    public static final String HORSE_RACE_PARTICIPANTS_CANNOT_BE_EMPTY = "Participants cannot be null or empty";
    public static final String HORSE_RACE_HORSE_COUNT_OUT_OF_RANGE = "Horse count must be between %d and %d, got: %d";
    public static final String HORSE_RACE_HORSE_COUNT_CANNOT_BE_NULL = "Horse count cannot be null";

    // -------------------------------------------------------------------------
    // Durak
    // -------------------------------------------------------------------------

    public static final String DURAK_GAME_NOT_FOUND = "Game not found";
}
