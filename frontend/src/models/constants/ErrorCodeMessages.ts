export const errorCodeMessages: Record<string, string> = {
    ROOM_NOT_FOUND: "Room not found",
    INVALID_MOVE: "Invalid move",
    NOT_YOUR_TURN: "It's not your turn",
    GAME_NOT_STARTED: "Game has not started yet",
    GAME_ALREADY_FINISHED: "Game has already finished",
    INSUFFICIENT_BALANCE: "Insufficient balance",
    ROOM_ALREADY_EXISTS: "Room with this name already exists",
    ROOM_TYPE_NOT_FOUND: "Unknown room type",
    COOLDOWN: "Too many requests. Please wait before trying again",
    VALIDATION_ERROR: "Validation error",
    INVALID_MESSAGE: "Invalid message received",
    SERVICE_UNAVAILABLE: "Service is temporarily unavailable",
    INTERNAL_SERVER_ERROR: "An unexpected error occurred",

    SESSION_REVOKED: "Your session has been revoked. Please sign in again",

    START_FAILED: "Failed to start the game. Please try again",
    BET_REJECT: "Your bet was rejected",

    WS_DISCONNECTED: "Connection lost. Trying to reconnect...",
    WS_ERROR: "Connection error. Please refresh the page",

    DEFAULT: "Something went wrong. Please try again",
};

export const systemErrorCodes = new Set([
    "INTERNAL_SERVER_ERROR",
    "SERVICE_UNAVAILABLE",
]);
