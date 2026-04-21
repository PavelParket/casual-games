package com.security_service.config;

public class ResourceMessageConstants {

    public static final String NOT_FOUND_USER_WITH_EMAIL = "User not found with email: %s";
    public static final String NOT_FOUND_USER_WITH_GUID = "User not found with guid: %s";
    public static final String NOT_FOUND_REFRESH_TOKEN = "Refresh token not found";
    public static final String NOT_FOUND_USER = "User not found";
    public static final String NOT_FOUND_PERMISSION = "Permission not found";
    public static final String NOT_FOUND_USER_PERMISSION = "User permission not found";
    public static final String NOT_FOUND_ROLE = "Role not found: %s";

    public static final String CONFLICT_USER_EMAIL = "User with email %s already exists";
    public static final String CONFLICT_USER_PERMISSION = "Permission already exists for this user";

    public static final String BAD_REQUEST_EMAIL_FORMAT = "Email should be valid: \"mail@example.com\"";
    public static final String BAD_REQUEST_INVALID_GUID = "Invalid user_guid format: %s";

    public static final String EXPIRED_TOKEN = "Token is expired";
    public static final String REQUIRED_PASSWORD = "Password cannot be null";

    public static final String FORBIDDEN_PASSWORD_UPDATE = "No permission to change password for this user";
}
