package com.bank_service.config;

public class ResourceMessageConstants {

    public static final String INVALID_PLAYERS_COUNT = "%s requires exactly 2 players";
    public static final String UNEQUAL_BETS = "Bets must be equal. Player 1: %s, Player 2: %s";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance. Player %s: balance=%s, bet=%s";
    public static final String NOT_FOUND_WINNER = "Winner not found in player list";
    public static final String NOT_FOUND_LOSER = "Loser not found in player list";
    public static final String BALANCE_OVERFLOW = "Balance would exceed maximum limit. Player %s: current=%s, max_possible=%s, limit=%s";
    public static final String INVALID_BET_AMOUNT = "Amount must be greater than 0";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds. Balance: %s, Required: %s";
    public static final String WINNER_GUID_MISMATCH = "Winner GUID does not match player GUID in payload";
    public static final String DEPOSIT_EXCEEDS_MAX_BALANCE = "Deposit rejected: balance must not be greater than %s after deposit";
    public static final String BAD_REQUEST_DEPOSIT_COOLDOWN = "Deposit rejected: please wait %02d:%02d before next deposit";

    public static final String ROOM_ALREADY_PROCESSED = "Room %s has already been processed";

    public static final String FORBIDDEN_READ_TRANSACTIONS = "Access denied: cannot read transactions for user: %s";
    public static final String FORBIDDEN_DEPOSIT = "Access denied: cannot deposit for user: %s";
    public static final String FORBIDDEN_READ_SUMMARY = "Access denied: cannot read summary for user: %s";
}
