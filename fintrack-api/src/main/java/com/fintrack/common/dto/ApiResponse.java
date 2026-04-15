package com.fintrack.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Standard API response wrapper for all endpoints.
 * Ensures consistent response structure across the entire API.
 *
 * @param <T> the type of data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        Instant timestamp,
        String path
) {

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data, null, Instant.now(), null);
    }

    /**
     * Creates a successful response with data and custom message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, Instant.now(), null);
    }

    /**
     * Creates a successful response with no data payload.
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, Instant.now(), null);
    }

    /**
     * Creates an error response with message and path.
     */
    public static <T> ApiResponse<T> error(String message, String path) {
        return new ApiResponse<>(false, message, null, null, Instant.now(), path);
    }

    /**
     * Creates an error response with message, errors map, and path.
     */
    public static <T> ApiResponse<T> error(String message, Object errors, String path) {
        return new ApiResponse<>(false, message, null, errors, Instant.now(), path);
    }
}
