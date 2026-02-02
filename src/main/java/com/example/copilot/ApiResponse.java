package com.example.copilot;

import java.time.Instant;

/**
 * Generic REST API response wrapper.
 * 
 * Usage example:
 * var success = ApiResponse.success(new User("john", "john@example.com"));
 * var error = ApiResponse.error("User not found");
 * System.out.println(success.data()); // User object
 */
public record ApiResponse<T>(
    T data,
    boolean success,
    String errorMessage,
    Instant timestamp
) {
    
    /**
     * Creates a successful API response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, true, null, Instant.now());
    }
    
    /**
     * Creates an error API response with a message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, false, message, Instant.now());
    }
}
