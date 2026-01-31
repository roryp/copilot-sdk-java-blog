import java.time.Instant;

/**
 * Generic API response wrapper for REST endpoints.
 *
 * @param <T> the type of the data payload
 * @param data the response payload (null on error)
 * @param success whether the request succeeded
 * @param errorMessage error description (null on success)
 * @param timestamp when the response was created
 *
 * Usage example:
 *   ApiResponse<User> ok = ApiResponse.success(new User("Alice"));
 *   ApiResponse<User> fail = ApiResponse.error("User not found");
 *   System.out.println(ok.success() + " at " + ok.timestamp());
 */
public record ApiResponse<T>(
    T data,
    boolean success,
    String errorMessage,
    Instant timestamp
) {
    /**
     * Creates a successful response with the given data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, true, null, Instant.now());
    }

    /**
     * Creates an error response with the given message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, false, message, Instant.now());
    }
}
