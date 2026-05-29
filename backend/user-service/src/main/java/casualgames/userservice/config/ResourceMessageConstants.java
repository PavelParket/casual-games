package casualgames.userservice.config;

public class ResourceMessageConstants {

    public static final String NOT_FOUND_USER = "User not found with guid: %s";
    public static final String CONFLICT_USER_EMAIL = "User with email %s already exists";
    public static final String DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER = "You do not have permission to update user";
    public static final String DO_NOT_HAVE_PERMISSION_TO_DELETE_USER = "You do not have permission to delete user";
    public static final String DO_NOT_HAVE_PERMISSION_TO_UPDATE_USER_ROLE = "You do not have permission to update user role";
    public static final String DO_NOT_HAVE_PERMISSION_TO_READ_USER_BALANCE = "You do not have permission to read user balance";
    public static final String ONE_OR_ANY_USERS_ARE_MISSING = "One or any users are missing";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance for user: %s";
    public static final String REQUIRED_TRANSACTION_LIST = "Transaction list must not be empty";
    public static final String REQUIRED_PENDING_STATUS_FOR_TRANSACTIONS = "All transactions must have PENDING status";
    public static final String REQUIRED_POSITIVE_TRANSACTION_AMOUNTS = "All transaction amounts must be positive";
    public static final String TOO_LARGE_UPLOADING_FILE = "File size exceeds the maximum allowed limit";
    public static final String INVALID_FILE_TYPE = "Invalid file type";
    public static final String UNSUPPORTED_FILE_TYPE = "Unsupported file type: %s. Allowed: %s";
    public static final String DO_NOT_HAVE_PERMISSION_TO_UPDATE_PROFILE_PICTURE = "You do not have permission to update profile picture";
    public static final String DO_NOT_HAVE_PERMISSION_TO_DELETE_PROFILE_PICTURE = "You do not have permission to delete profile picture";
    public static final String FILES_ARE_MISSING = "All files are not present";
    public static final String NOT_FOUND_SUBSCRIPTION_PLAN = "Subscription plan not found for status: %s";
    public static final String NOT_FOUND_SUBSCRIPTION = "Subscription not found for user: %s";
    public static final String CONFLICT_SAME_TIER_SUBSCRIPTION = "You already have an active %s subscription";
    public static final String BAD_REQUEST_NO_NECESSARY_BALANCE_AMOUNT = "You balance have no necessary balance amount";
    public static final String DO_NOT_HAVE_PERMISSION_TO_READ_SUBSCRIPTION = "You do not have permission to read subscription";
    public static final String DO_NOT_HAVE_PERMISSION_TO_UPDATE_SUBSCRIPTION = "You do not have permission to update subscription";
}
