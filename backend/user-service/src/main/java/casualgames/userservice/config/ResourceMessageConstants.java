package casualgames.userservice.config;

public class ResourceMessageConstants {

    public static final String INTERNAL_SERVER_ERROR = "Internal server error. Please try again";
    public static final String ONE_OR_ANY_USERS_ARE_MISSING = "One or any users are missing";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance for user: %s";
    public static final String REQUIRED_TRANSACTION_LIST = "Transaction list must not be empty";
    public static final String REQUIRED_PENDING_STATUS_FOR_TRANSACTIONS = "All transactions must have PENDING status";
    public static final String REQUIRED_POSITIVE_TRANSACTION_AMOUNTS = "All transaction amounts must be positive";
    public static final String CONFLICT_USER_EMAIL = "User with email '%s' already exists";
    public static final String NOT_FOUND_USER = "User not found with guid: %s";

}
