package com.fintrack.auth.dto;

/**
 * Response DTO for authentication (login/refresh) containing tokens and user info.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
