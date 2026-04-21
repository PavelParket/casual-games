package com.bank_service.config;

public class ResourceMessageConstants {

    public static final String UNSUPPORTED_ROOM_TYPE = "No processor found for room type: %s";
    public static final String BAD_REQUEST_TYPE = "Invalid request type for %s";

    public static final String ROOM_ALREADY_PROCESSED = "Room %s has already been processed";

    public static final String FORBIDDEN_READ_TRANSACTIONS = "Access denied: cannot read transactions for user: %s";
    public static final String FORBIDDEN_DEPOSIT = "Access denied: cannot deposit for user: %s";
    public static final String FORBIDDEN_READ_SUMMARY = "Access denied: cannot read summary for user: %s";
}
